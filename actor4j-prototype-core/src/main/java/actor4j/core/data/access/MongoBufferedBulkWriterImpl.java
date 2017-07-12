package actor4j.core.data.access;

import java.util.LinkedList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.WriteModel;

public class MongoBufferedBulkWriterImpl implements MongoBufferedBulkWriter {
	protected MongoCollection<Document> collection;
	
	protected List<WriteModel<Document>> requests;
	protected boolean ordered;
	
	protected int size;
	protected int counter;
	
	public MongoBufferedBulkWriterImpl(MongoCollection<Document> collection, boolean ordered, int size) {
		super();
		this.collection = collection;
		this.ordered = ordered;
		this.size = size;
		
		counter = 0;
		requests = new LinkedList<>();
	}
	
	@Override
	public void write(WriteModel<Document> request) {
		requests.add(request);
		counter++;
		if (counter==size)
			flush();
	}
	
	@Override
	public void flush() {
		if (ordered)
			collection.bulkWrite(requests);
		else
			collection.bulkWrite(requests, new BulkWriteOptions().ordered(false));
		
		requests.clear();
		counter = 0;
	}
}
