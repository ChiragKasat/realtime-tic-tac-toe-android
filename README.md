
# Journal App

Project Name:  `Tic Tac Toe`  
Name:  `Chirag Kasat`
Email:  `chiragkasat0@gmail.com`

## Description
This a online multiplayer rendition of the popular game Tic-Tac-Toe. The game has two modes to play in; 1. Single player mode where you play against the computer, and 2. Multiplayer mode where you can play against a random player online. It maintains a record of your wins and losses and registers you using your email ID.

## Known Bugs
- When number of players have a multiplayer game open, the list of open games becomes tremendously huge and auto updates itself. So paging needs to be done for this.
- Signing out in between the game will result in erroneous behaviour.

## Pictures
![Screenshot](/screenshots/1.png)

![Screenshot](/screenshots/2.png)

## Brief Approach

### Authentication
Used firebase auth to implement this feature. The login fragment is opened if there's no user logged in and then he enters his credentials and a new user is created. It checks if the user already exists. If so, it logs him in instead of creating a new one.
### Single Player mode
Maintained a activePlayer variable and gameState array which shows whose turn it is and what the state of game is. It checks for wins or ties after each turn and shows a appropriate dialog and increments wins or loss for the user. The record is maintained in firebase realtime database.
### Multiplayer mode
If user uses the FAB to create a 2-player game, it generates a unique ID of game and enlists itself in the available games section on Dasboard on other devices. If someone clicks on the view from dashboard to enter the game, the game is removed from active record and a new record is made in firebase database for the game state, which is then shared across the 2 users. This functionality is not working properly in the code.
### Sharing Journal
User can press the share button which launches an implicit plain text intent so all the apps handling text data pops up for user to choose from.