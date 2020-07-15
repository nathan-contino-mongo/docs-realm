const app = new Realm.App({ id: "myapp-abcde" });

// Get all on-device users
const users = app.allUsers();

// You can only switch to a logged-in users
const authenticatedUsers = users.filter(user => user.isLoggedIn);
const user1 = authenticatedUsers[0];
const user2 = authenticatedUsers[1];

// Switch to user1
app.switchUser(user1);
// The active user is now user1
assert(app.currentUser.id === user1.id);

// Switch to user2
app.switchUser(user2);
// The active user is now user2
assert(app.currentUser.id === user2.id);
