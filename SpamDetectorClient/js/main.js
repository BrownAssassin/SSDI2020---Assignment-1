(function () {
  // Listing all the test files
  fetch("http://localhost:8080/spamDetector-1.0/api/spam", {
    method:'GET',
    headers: {
      'Accept': 'application/json'
    },
  })
    .then((response) => response.json())
    .then((response) => {
      console.log("Loaded data from http://localhost:8080/spamDetector-1.0/api/spam:");
      console.log(response);
      return response;
    })
    .then((response) => {
      for (let result in response) {
        updateTable(response[result]);
      }
    })
    .catch((err) => {
      console.log("Something went wrong: " + err);
    });

  // Calculate and get accuracy
  fetch("http://localhost:8080/spamDetector-1.0/api/spam/accuracy", {
    method:'GET',
    headers: {
      'Accept': 'application/json'
    },
  })
    .then((response) => response.json())
    .then((response) => {
      console.log("Loaded data from http://localhost:8080/spamDetector-1.0/api/spam/accuracy:");
      console.log(response);
      return response;
    })
    .then((response) => {
      for (let result in response) {
        updatePerformance("accuracy", response);
      }
    })
    .catch((err) => {
      console.log("Something went wrong: " + err);
    });

  // Calculate and get precision
  fetch("http://localhost:8080/spamDetector-1.0/api/spam/precision", {
    method:'GET',
    headers: {
      'Accept': 'application/json'
    },
  })
    .then((response) => response.json())
    .then((response) => {
      console.log("Loaded data from http://localhost:8080/spamDetector-1.0/api/spam/precision:");
      console.log(response);
      return response;
    })
    .then((response) => {
      for (let result in response) {
        updatePerformance("precision", response);
      }
    })
    .catch((err) => {
      console.log("Something went wrong: " + err);
    });
})();

function updateTable(newEntry) {
  const newRow = document.getElementById("chart").insertRow(-1);

  newRow.insertCell(0).innerHTML = newEntry.file;
  newRow.insertCell(1).innerHTML = newEntry.spamProbability;
  newRow.insertCell(2).innerHTML = newEntry.actualClass;
}

function updatePerformance(type, value) {
  if (type === "accuracy") {
    document.getElementById("accuracy").placeholder = value.val;
  } else if (type === "precision") {
    document.getElementById("precision").placeholder = value.val;
  } else {
    console.log("Incorrect type provided for `updatePerformance(type, value)` method.")
  }
}
