1. Guess function, the guessed number is in the range of 0-9, if the client send the right answer, the score +1, otherwise the server will send the hint for the client send again.
2. if the parameters of the request from the client is not the format, or not the valid number  or the number of param is not 1, the server will stop the connection immediately.
The initial score is 0.
Function> Guess
Guess (0-9)? 5
Guess too low. Guess (0-9)? 7
Guess too low. Guess (0-9)? 9
Guess too high. Guess (0-9)? 8
Correct! Score 2