1. Use AsynchronousSocketChannel to implement a TCP server.
2. Create a TCP server containing a UDP server using the same port.
3. TCP server is used to store the application information like use counts.
4. TCP server uses atomicinteger to add counts.
5. UDP server is used to return thr applcation information stored in the TCP sever.
6. UDP using the custom format to send and receive data.
7. Both tcp message and udp message use factory methods to create. 