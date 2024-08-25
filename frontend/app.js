document.addEventListener("DOMContentLoaded", function () {
  // Elements
  const loginPage = document.getElementById("loginPage");
  const registerPage = document.getElementById("registerPage");
  const mainPage = document.getElementById("mainPage");

  const loginButton = document.getElementById("loginButton");
  const registerButton = document.getElementById("registerButton");
  const goToRegister = document.getElementById("goToRegister");
  const goToLogin = document.getElementById("goToLogin");

  const userList = document.getElementById("userList");
  const chatMessages = document.getElementById("chatMessages");
  const messageInput = document.getElementById("messageInput");
  const sendMessageButton = document.getElementById("sendMessageButton");
  const chatHeader = document.getElementById("chatHeader");
  const logoutButton = document.getElementById("logoutButton");

  const uploadTriggerButton = document.getElementById("uploadTriggerButton");
  const fileInput = document.getElementById("fileInput");
  const sendFileButton = document.getElementById("sendFileButton");

  let currentUser = null; // This will be populated after login
  let selectedUser = null; // Selected user to chat with
  let socket = null; // WebSocket connection
  let stompClient = null;
  let currentSubscription = null;

  // Function to show a specific page and hide the others
  function showPage(pageToShow) {
    loginPage.style.display = "none";
    registerPage.style.display = "none";
    mainPage.style.display = "none";

    pageToShow.style.display = "block";
  }

  // Show the login page by default
  showPage(loginPage);

  // Event listeners for switching between pages
  goToRegister.addEventListener("click", function (event) {
    event.preventDefault();
    showPage(registerPage);
  });

  goToLogin.addEventListener("click", function (event) {
    event.preventDefault();
    showPage(loginPage);
  });

  // Function to encode credentials for Basic Auth
  function getBasicAuthHeader(username, password) {
    const credentials = `${username}:${password}`;
    return `Basic ${btoa(credentials)}`;
  }

  // Function to register a new user
  async function register() {
    const username = document.getElementById("registerUsername").value;
    const password = document.getElementById("registerPassword").value;

    try {
      const response = await fetch("http://localhost:8080/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, password }),
      });

      if (response.ok) {
        alert("Registration successful! You can now log in.");
        showPage(loginPage);
      } else {
        const errorMessage = await response.text();
        console.error("Registration failed:", errorMessage);
        alert("Registration failed: " + errorMessage);
      }
    } catch (error) {
      console.error("Error occurred during registration:", error);
    }
  }

  // Function to login a user
  async function login() {
    const username = document.getElementById("loginUsername").value;
    const password = document.getElementById("loginPassword").value;

    try {
      const authHeader = getBasicAuthHeader(username, password);

      const response = await fetch("http://localhost:8080/login", {
        method: "POST",
        headers: {
          Authorization: authHeader,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, password }),
      });

      if (response.ok) {
        currentUser = { username, password }; // Store credentials

        // Display the current user's name in the header
        document.getElementById(
          "userGreeting"
        ).textContent = `Logged in as ${currentUser.username}`;

        showPage(mainPage);
        initializeChatApp();
      } else {
        const errorMessage = await response.text();
        console.error("Login failed:", errorMessage);
        alert("Login failed: " + errorMessage);
      }
    } catch (error) {
      console.error("Login Error:", error);
    }
  }

  // Initialize chat app after login
  async function initializeChatApp() {
    await loadUsers();
    if (!socket || !stompClient || !stompClient.connected) {
      console.log("Attempting to connect WebSocket...");
      connectWebSocket();
    }
  }

  // Function to load all users
  async function loadUsers() {
    try {
      const authHeader = getBasicAuthHeader(
        currentUser.username,
        currentUser.password
      );
      const response = await fetch("http://localhost:8080/users", {
        headers: {
          Authorization: authHeader,
        },
      });
      const users = await response.json();
      userList.innerHTML = "";
      users.forEach((user) => {
        if (user.username !== currentUser.username) {
          const li = document.createElement("li");
          li.textContent = user.username;
          li.dataset.userId = user.id;
          li.addEventListener("click", () => selectUser(user));
          userList.appendChild(li);
        }
      });
    } catch (error) {
      console.error("Error loading users:", error);
    }
  }

  // Select User and Load Chat
  async function selectUser(user) {
    selectedUser = user;
    chatHeader.textContent = `Chat with ${user.username}`;
    chatMessages.innerHTML = ""; // Clear previous messages
    await loadChatMessages(user.id);
  }
  // Add an event listener to trigger the search as the user types
  let debounceTimer;
  document.getElementById("searchInput").addEventListener("input", () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(filterUserList, 300); // Adjust the delay as needed
  });
  // Function to search users based on the input
  async function filterUserList() {
    const searchInput = document
      .getElementById("searchInput")
      .value.toLowerCase();

    if (searchInput.trim() === "") {
      loadUsers(); // Load all users if search input is empty
      return;
    }

    try {
      const authHeader = getBasicAuthHeader(
        currentUser.username,
        currentUser.password
      );
      const response = await fetch(
        `http://localhost:8080/users/search?query=${encodeURIComponent(
          searchInput
        )}`,
        {
          headers: {
            Authorization: authHeader,
          },
        }
      );

      if (response.ok) {
        const users = await response.json();
        userList.innerHTML = ""; // Clear the user list before displaying search results

        users.forEach((user) => {
          const li = document.createElement("li");
          li.textContent = user.username;
          li.dataset.userId = user.id;
          li.addEventListener("click", () => selectUser(user));
          userList.appendChild(li);
        });
      } else {
        console.error("Failed to search users");
      }
    } catch (error) {
      console.error("Error searching users:", error);
    }
  }

  // Add event listener to the search input to filter users on input
  document
    .getElementById("searchInput")
    .addEventListener("input", filterUserList);

  // Function to load previous chat messages
  async function loadChatMessages(userId) {
    try {
      const authHeader = getBasicAuthHeader(
        currentUser.username,
        currentUser.password
      );
      const response = await fetch(
        `http://localhost:8080/chat/messages/${userId}`,
        {
          headers: {
            Authorization: authHeader,
          },
        }
      );
      if (response.ok) {
        const messages = await response.json();
        console.log("Loaded messages:", messages); // Debugging statement
        chatMessages.innerHTML = ""; // Clear previous messages
        messages.forEach((msg) => {
          const messageElement = document.createElement("div");
          messageElement.textContent = `${msg.sender}: ${msg.content}`;
          chatMessages.appendChild(messageElement);
        });
      } else {
        console.error("Failed to load messages");
      }
    } catch (error) {
      console.error("Error loading messages:", error);
    }
  }

  // Send a message
  async function sendMessage() {
    if (selectedUser && messageInput.value.trim() !== "") {
      const messageContent = messageInput.value;
      try {
        const authHeader = getBasicAuthHeader(
          currentUser.username,
          currentUser.password
        );
        const response = await fetch("http://localhost:8080/chat/send", {
          method: "POST",
          headers: {
            Authorization: authHeader,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            sender: currentUser.username,
            receiver: selectedUser.username,
            content: messageContent,
          }),
        });

        if (response.ok) {
          messageInput.value = ""; // Clear input field
          // No need to manually call displayMessage, WebSocket will handle it
        } else {
          console.error("Failed to send message");
          alert("Failed to send message");
        }
      } catch (error) {
        console.error("Error sending message:", error);
      }
    }
  }

  // Trigger file input when "+" button is clicked
  uploadTriggerButton.addEventListener("click", function () {
    fileInput.click(); // Programmatically click the hidden file input
  });

  // Show send file button after selecting a file
  fileInput.addEventListener("change", function () {
    if (fileInput.files.length > 0) {
      sendFileButton.style.display = "inline-block";
    } else {
      sendFileButton.style.display = "none";
    }
  });

  // Handle file upload when "Send File" button is clicked
  sendFileButton.addEventListener("click", function () {
    const file = fileInput.files[0];
    if (!file) {
      alert("Please select a file first!");
      return;
    }
    const uploader = currentUser.username;
    const receiver = selectedUser.username;
    uploadFile(file, uploader, receiver);
  });

  // Function to upload a file
  async function uploadFile(file, uploader, receiver) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("uploader", uploader);
    formData.append("receiver", receiver);

    try {
      const authHeader = getBasicAuthHeader(
        currentUser.username,
        currentUser.password
      );
      const response = await fetch("http://localhost:8080/files/upload", {
        method: "POST",
        headers: {
          Authorization: authHeader,
        },
        body: formData,
      });

      if (response.ok) {
        const fileInfo = await response.json();
        console.log("File uploaded successfully:", fileInfo);
        displayMessage(uploader, `File "${file.name}" uploaded successfully.`);
        // displayMessage(
        //   receiver,
        //   `File "${file.name}" downloaded successfully.`
        // );
        sendFileButton.style.display = "none"; // Hide the button after upload
      } else {
        console.error("Failed to upload file");
        alert("Failed to upload file");
      }
    } catch (error) {
      console.error("Error uploading file:", error);
      alert("Error uploading file");
    }
  }

  // Function to download a file
  async function downloadFile(fileId, receiver) {
    console.log(`something is downloading`);
    try {
      const authHeader = getBasicAuthHeader(
        currentUser.username,
        currentUser.password
      );
      const response = await fetch(
        `http://localhost:8080/files/download/${fileId}?receiver=${encodeURIComponent(
          receiver
        )}`,
        {
          method: "GET",
          headers: {
            Authorization: authHeader,
          },
        }
      );

      if (response.ok) {
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = downloadUrl;
        a.download = ""; // Filename will be set automatically by the server
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(downloadUrl);
        displayMessage(currentUser.username, `hellp`);
        displayMessage(currentUser.username, `File downloaded successfully.`);
        sendDownloadNotification(fileName, receiver);
      } else {
        console.error("Failed to download file");
        alert("Failed to download file");
      }
    } catch (error) {
      console.error("Error downloading file:", error);
      alert("Error downloading file");
    }
  }

  // Function to send a WebSocket notification after download
  function sendDownloadNotification(fileName, receiver) {
    if (stompClient && stompClient.connected) {
      const downloadMessage = {
        sender: currentUser.username,
        receiver: receiver,
        content: `File "${fileName}" downloaded successfully.`,
      };
      stompClient.send("/app/chat", {}, JSON.stringify(downloadMessage));
    }
  }

  // Connect to WebSocket
  //let currentSubscription = null;

  function connectWebSocket() {
    if (stompClient !== null) {
      stompClient.disconnect();
    }

    socket = new SockJS("http://localhost:8080/ws");
    stompClient = Stomp.over(socket);

    stompClient.connect(
      {},
      function (frame) {
        console.log("WebSocket connected");
        subscribeToUserMessages();
      },
      function (error) {
        console.error("WebSocket error:", error);
        setTimeout(connectWebSocket, 3000);
      }
    );
  }

  function subscribeToUserMessages() {
    if (currentSubscription) {
      currentSubscription.unsubscribe();
    }

    currentSubscription = stompClient.subscribe(
      `/user/${currentUser.username}/queue/messages`,
      function (message) {
        const msg = JSON.parse(message.body);
        displayMessageIfNotDuplicate(msg.sender, msg.content);
      }
    );
  }
  // Function to check for duplicates and display the message
  const recentMessages = new Set();
  function displayMessageIfNotDuplicate(sender, content) {
    const messageKey = `${sender}:${content}`;
    console.log(`Received message: ${messageKey}`);

    if (recentMessages.has(messageKey)) {
      console.warn(`Duplicate message detected: ${messageKey}`);
      return;
    }

    recentMessages.add(messageKey);
    if (recentMessages.size > 100) {
      // Limit the size of recentMessages
      recentMessages.delete(recentMessages.values().next().value);
    }
    displayMessage(sender, content);
  }
  // Display the message in the chat window
  function displayMessage(sender, content) {
    const messageElement = document.createElement("div");
    messageElement.textContent = `${sender}: ${content}`;
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight; // Scroll to bottom
  }

  // Logout Functionality
  logoutButton.addEventListener("click", async function () {
    try {
      const response = await fetch("http://localhost:8080/logout", {
        method: "POST",
        headers: {
          Authorization: getBasicAuthHeader(
            currentUser.username,
            currentUser.password
          ),
        },
      });

      if (response.ok) {
        currentUser = null;
        alert("Logged out");
        showPage(loginPage);
      } else {
        alert("Logout failed");
      }
    } catch (error) {
      console.error("Error logging out:", error);
      alert("Error logging out");
    }
  });

  // Event Listeners
  loginButton.addEventListener("click", login);
  registerButton.addEventListener("click", register);
  sendMessageButton.addEventListener("click", sendMessage);
  messageInput.addEventListener("keypress", function (event) {
    if (event.key === "Enter") {
      event.preventDefault();
      sendMessage();
    }
  });
});
