# messenger
# This is messenger server and client
messenger server supports sending messages to your friends
messager server supports maintaining contact list and allows to add and remove friends
messenger client is a simple program that when started first register user then logs in and then waits to send messages at the same time waiting to receive messages

# Dev set up
messenger is based on GRPC protocol

install protobuf compiler <br>
install maven <br>
git clone https://github.com/mansimahendru/messenger.git <br>
open command window. run mvn clean package install <br>

to start server open new terminal and execute <br>
mvn exec:java -Dexec.mainClass=com.messenger.server.MessengerServer <br>
to start client open new terminal and execute <br>
mvn exec:java -Dexec.mainClass=com.messenger.client.MessengerClient -Dexec.args="userid" <br>
to send message type following in client terminal <br>
userid:message <br>
