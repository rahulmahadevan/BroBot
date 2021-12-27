import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DBOps {
    private static final String HOST = Utils.HOST;
    private static final int PORT = Utils.PORT;
    private static final String DATABASE = Utils.DATABASE;
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static final String C_ANNOUNCEMENT = Utils.COLLECTION_ANNOUNCEMENT;
    private static final String C_TIME_TABLE = Utils.COLLECTION_TIME_TABLE;
    private static DateFormat dateFormat;

    public DBOps(){
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            mongoClient = new MongoClient(HOST, PORT);
            database = mongoClient.getDatabase(DATABASE);
            System.out.println("Connected to Database");
        }catch (MongoException e){
            e.printStackTrace();
        }
    }

    public void DBClose(){
        mongoClient.close();
        System.out.println("MongoDB connection closed");
    }

    public String setAnnouncement(String[] data) throws ParseException {
        MongoCollection collection = database.getCollection(C_ANNOUNCEMENT);
        System.out.println("Setting New Announcement");
        Date date = dateFormat.parse(data[3]);
        try {
            collection.insertOne(new Document()
                    .append("title", data[0])
                    .append("description", data[1])
                    .append("dateMeaning",data[2])
                    .append("date", date)
                    .append("time", data[4])
                    .append("status","active"));
            System.out.println("Announcement added successfully");
            String announce = "Announcement! \uD83D\uDCE2 \n\n" +
                    data[0] + "\n" +
                    data[1] + "\n" +
                    data[2] + ": " +dateFormat.format(date) + "\n" +
                    data[4];
            return announce;

        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public List<String> getAnnouncements(){
        ArrayList<String> result = new ArrayList<>();
        MongoCollection collection = database.getCollection(C_ANNOUNCEMENT);
        BasicDBObject query = new BasicDBObject();
        query.put("date", new BasicDBObject("$gte", new Date()));
        MongoCursor<Document> cursor = collection.find(query).sort(new BasicDBObject("date", 1)).iterator();
        while(cursor.hasNext()){
            Document doc = cursor.next();
            Date date = doc.getDate("date");
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DAY_OF_YEAR, 1);
            Calendar currentdate = Calendar.getInstance();
            currentdate.setTime(date);
            if(tomorrow.get(Calendar.YEAR) == currentdate.get(Calendar.YEAR)
                    && tomorrow.get(Calendar.DAY_OF_YEAR) == currentdate.get(Calendar.DAY_OF_YEAR)){
                ObjectId id = doc.getObjectId("_id");
                String status = doc.getString("status");
                if(status == null){
                    continue;
                }
                if(status.equals("inactive")){
                    continue;
                }
                String title = doc.getString("title");
                String description = doc.getString("description");
                String time = doc.getString("time");
                String dateMeaning = doc.getString("dateMeaning");
                String reminder = "Remainder! ⏰ \n\n" +
                        title + "\n" +
                        description + "\n" +
                        dateMeaning + ": " +dateFormat.format(date) + "\n" +
                        time;
                result.add(reminder);
                Document findQuery = new Document().append("_id", id);
                Bson updates = Updates.combine(Updates.set("status", "inactive"));
                UpdateOptions options = new UpdateOptions().upsert(true);
                try{
                    collection.updateOne(findQuery, updates, options);
                    System.out.println("Reminder completed for document "+String.valueOf(id));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
