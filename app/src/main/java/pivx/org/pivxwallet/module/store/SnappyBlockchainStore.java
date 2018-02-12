package pivx.org.pivxwallet.module.store;

import android.support.annotation.Nullable;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.pivxj.core.Block;
import org.pivxj.core.Context;
import org.pivxj.core.NetworkParameters;
import org.pivxj.core.Sha256Hash;
import org.pivxj.core.StoredBlock;
import org.pivxj.store.BlockStore;
import org.pivxj.store.BlockStoreException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by furszy on 10/17/17.
 */

public class SnappyBlockchainStore implements BlockStore{

    private static final String CHAIN_HEAD_KEY_STRING = "chainhead";

    private final Context context;
    private DB db;
    private final ByteBuffer buffer = ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE);
    private final ByteBuffer zerocoinBuffer = ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE_ZEROCOIN);
    private final File path;
    private final String filename;


    /** Creates a LevelDB SPV block store using the given factory, which is useful if you want a pure Java version. */
    public SnappyBlockchainStore(Context context, File directory,String filename) throws BlockStoreException {
        this.context = context;
        this.path = directory;
        this.filename = filename;
        try {
            tryOpen(directory, filename);
        } catch (IOException e) {
            throw new BlockStoreException(e);
            /*try {
                dbFactory.repair(directory, options);
                tryOpen(directory, dbFactory, options);
            } catch (IOException e1) {
                throw new BlockStoreException(e1);
            }*/
        }
    }

    private synchronized void tryOpen(File directory,String filename) throws IOException, BlockStoreException {
        try {
            db = DBFactory.open(directory.getAbsolutePath(),filename);
        } catch (SnappydbException e) {
            throw new IOException(e);
        }
        initStoreIfNeeded();
    }

    private synchronized void initStoreIfNeeded() throws BlockStoreException {
        try {
            if (db.getBytes(CHAIN_HEAD_KEY_STRING) != null)
                return;   // Already initialised.
        } catch (SnappydbException e) {
            // not initialized
            Block genesis = context.getParams().getGenesisBlock().cloneAsHeader();
            StoredBlock storedGenesis = new StoredBlock(genesis, genesis.getWork(), 0);
            put(storedGenesis);
            setChainHead(storedGenesis);
        }
    }

    @Override
    public synchronized void put(StoredBlock block) throws BlockStoreException {
        try {
            //System.out.println("### trying to save something..");
            ByteBuffer buffer;
            buffer = block.getHeader().isZerocoin() ? zerocoinBuffer : this.buffer;
            buffer.clear();
            //System.out.println("Block information: " + block.toString());
            block.serializeCompact(buffer);
            Sha256Hash blockHash = block.getHeader().getHash();
            //System.out.println("### block hash to save: " + blockHash.toString());
            //byte[] hash = blockHash.getBytes();
            byte[] dbBuffer = buffer.array();
            db.put(blockHash.toString(), dbBuffer);
            // just for now to check something:
            StoredBlock dbBlock = get(blockHash);

            assert Arrays.equals(dbBlock.getHeader().getHash().getBytes(), blockHash.getBytes()) : "put is different than get in db.. " + block.getHeader().getHashAsString() + ", db: " + dbBlock.getHeader().getHashAsString();
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new BlockStoreException(e);
        }
    }

    @Override @Nullable
    public synchronized StoredBlock get(Sha256Hash hash) throws BlockStoreException {
        try {
            String blockToGet = hash.toString();
            if (!db.exists(blockToGet)) {
                //System.out.println("Block to get doesn't exists: "+blockToGet);
                return null;
            }
            byte[] bits = db.getBytes(blockToGet);
            if (bits == null)
                return null;
            return StoredBlock.deserializeCompact(context.getParams(), ByteBuffer.wrap(bits));
        } catch (SnappydbException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public synchronized StoredBlock getChainHead() throws BlockStoreException {
        try {
            System.out.println("Calling get Method from chain head");
            return get(Sha256Hash.wrap(db.getBytes(CHAIN_HEAD_KEY_STRING)));
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new BlockStoreException(e);
        }
    }

    @Override
    public synchronized void setChainHead(StoredBlock chainHead) throws BlockStoreException {
        try {
            db.put(CHAIN_HEAD_KEY_STRING, chainHead.getHeader().getHash().getBytes());
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new BlockStoreException(e);
        }
    }

    @Override
    public synchronized void close() throws BlockStoreException {
        try {
            db.destroy();
        } catch (SnappydbException e) {
            throw new BlockStoreException(e);
        }
    }

    public File getPath() {
        return path;
    }

    public String getFilename() {
        return filename;
    }

    /** Erases the contents of the database (but NOT the underlying files themselves) and then reinitialises with the genesis block.
    public synchronized void reset() throws BlockStoreException {
        try {
            WriteBatch batch = db.createWriteBatch();
            try {
                DBIterator it = db.iterator();
                try {
                    it.seekToFirst();
                    while (it.hasNext())
                        batch.delete(it.next().getKey());
                    db.write(batch);
                } finally {
                    it.close();
                }
            } finally {
                batch.close();
            }
            initStoreIfNeeded();
        } catch (IOException e) {
            throw new BlockStoreException(e);
        }
    }

    public synchronized void destroy() throws IOException {

        JniDBFactory.factory.destroy(path, new Options());
    }*/



    @Override
    public NetworkParameters getParams() {
        return context.getParams();
    }

    public void truncate() throws SnappydbException {
        db.destroy();
    }
}
