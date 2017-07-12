package actor4j.core.data.access;

import org.bson.Document;
import com.mongodb.client.model.WriteModel;

public interface MongoBufferedBulkWriter {
	public void write(WriteModel<Document> request);
	public void flush();
}
