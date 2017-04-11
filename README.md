# messenger
# Messenger Server and Client
messenger server supports sending messages to your friends <br>
messager server supports maintaining contact list and allows to add and remove friends <br>
messenger client is a simple program that when started first register user then logs in and then waits to send messages at the same time waiting to receive messages <br>

Current implementation have User object. User object stores list of users as friends and it is the contact list for user. <br>
User object also stores sessionid to enable login/logout functionality and maintaining session with client. <br>

Current implementation stores messages sent by various users into mongodb. <br>
Each user runs retrieve thread and continuously get messages from server. <br>
Once message is delivered to user, it is deleted from permanent storage. <br>

# Dev set up
messenger is based on gRPC protocol

install protobuf compiler <br>
install mongodb <br>
install maven <br>
git clone https://github.com/mansimahendru/messenger.git <br>
open command window. run mvn clean package install <br>

to start server open new terminal and execute <br>
mvn exec:java -Dexec.mainClass=com.messenger.server.MessengerServer <br>
to start client open new terminal and execute <br>
mvn exec:java -Dexec.mainClass=com.messenger.client.MessengerClient -Dexec.args="userid" <br>
to send message type following in client terminal <br>
userid:message <br>

# TODO
Users needs to be stored on mongodb. This is work in progress. <br>
Need to add test cases. <br>
