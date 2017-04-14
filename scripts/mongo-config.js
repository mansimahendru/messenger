var db = connect("localhost:27017/messengerdb"), results;
print('test1db Database created');
db.createCollection("user");
print('user collection created');
db.createCollection("messages");
print('messages collection created');