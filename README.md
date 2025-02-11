# Crazy 8s Networked Game
This project is an implementation of the classic Crazy 8s card game with networked multiplayer functionality. The game allows up to four players to connect and play in real-time over a network using UDP networking for communication.

## Features
* Multiplayer Gameplay: Supports up to four players in an online session.
* UDP Networking: Utilizes UDP sockets for fast and efficient communication between clients and the server.
* Turn-Based Mechanics: Handles player turns, enforcing game rules such as wild cards, draw twos, and skips.
* Game State Management: Tracks player actions, scores, and game progression.
* Automated Server Management: The server waits for players to join, starts the game, and manages rounds until a winner is determined.
## How It Works
* The Game Server waits for four players to connect.
* Once connected, the server assigns player IDs and distributes cards.
*Players take turns playing cards following standard Crazy 8s rules.
* The server sends updates on the game state, including the top card, turn order, and player actions.
* The game continues until a player wins by playing all their cards or reaching the score limit.
## Technologies Used
* Java for backend logic and networking
* UDP sockets for player communication
* Multi-threading for handling multiple clients simultaneously
* This project showcases real-time multiplayer networking and game development principles using Java.
