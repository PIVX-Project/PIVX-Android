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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chain.TruncableStore;
import host.furszy.zerocoinj.store.RollbackBlockStore;

/**
 * Created by furszy on 10/17/17.
 */

public class SnappyBlockchainStore implements BlockStore, RollbackBlockStore, TruncableStore {

    private static final String CHAIN_HEAD_KEY_STRING = "chainhead";
    private static final Logger log = LoggerFactory.getLogger(SnappyBlockchainStore.class);

    private final Context context;
    private DB db;
    private final ByteBuffer buffer = ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE);
    private final ByteBuffer zerocoinBuffer = ByteBuffer.allocate(StoredBlock.COMPACT_SERIALIZED_SIZE_ZEROCOIN);
    private final File path;
    private final String filename;

    // Stored genesis
    private StoredBlock storedGenesis;


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
            if (db == null || !db.isOpen())
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
            storedGenesis = new StoredBlock(genesis, genesis.getWork(), 0);
            put(storedGenesis);
            setChainHead(storedGenesis);
        }
    }

    @Override
    public synchronized void put(StoredBlock block) throws BlockStoreException {
        boolean notOpen = false;
        try {
            if (!db.isOpen()){
                try {
                    tryOpen(this.path, this.filename);
                    notOpen = true;
                } catch (IOException e) {
                    log.error("Error trying to open db", e);
                }
            }
            ByteBuffer buffer;
            buffer = block.getHeader().isZerocoin() ? zerocoinBuffer : this.buffer;
            buffer.clear();
            block.serializeCompact(buffer);
            Sha256Hash blockHash = block.getHeader().getHash();
            byte[] dbBuffer = buffer.array();
            db.put(blockHash.toString(), dbBuffer);
            // Do not remove this comments
            //StoredBlock dbBlock = get(blockHash);
            //assert Arrays.equals(dbBlock.getHeader().getHash().getBytes(), blockHash.getBytes()) : "put is different than get in db.. " + block.getHeader().getHashAsString() + ", db: " + dbBlock.getHeader().getHashAsString();
        } catch (SnappydbException e) {
            log.error("cannot store block", e);
            throw new BlockStoreException(e);
        }finally {
            if (notOpen){
                try {
                    close();
                }catch (Exception e){
                    log.error("Trying to close the blockstore after add a block because of service stopped", e);
                }
            }
        }
    }

    @Override @Nullable
    public synchronized StoredBlock get(Sha256Hash hash) throws BlockStoreException {
        boolean notOpen = false;
        try {
            if (!db.isOpen()){
                try {
                    tryOpen(this.path, this.filename);
                    notOpen = true;
                } catch (IOException e) {
                    log.error("Error trying to open db", e);
                }
            }

            String blockToGet = hash.toString();
            if (!db.exists(blockToGet)) {
                return null;
            }
            byte[] bits = db.getBytes(blockToGet);
            if (bits == null)
                return null;
            return StoredBlock.deserializeCompact(context.getParams(), ByteBuffer.wrap(bits));
        } catch (SnappydbException e) {
            log.error("Cannot get storedblock", e);
            return null;
        } finally {
            if (notOpen){
                try {
                    close();
                }catch (Exception e){
                    log.error("Trying to close the blockstore after get a block because of service stopped", e);
                }
            }
        }
    }

    @Override
    public synchronized StoredBlock getChainHead() throws BlockStoreException {
        boolean notOpen = false;
        try {
            if (!db.isOpen()){
                try {
                    tryOpen(this.path, this.filename);
                    notOpen = true;
                } catch (IOException e) {
                    log.error("Error trying to open db on getChainHead", e);
                }
            }

            return get(Sha256Hash.wrap(db.getBytes(CHAIN_HEAD_KEY_STRING)));
        } catch (SnappydbException e) {
            throw new BlockStoreException(e);
        } finally {
            if (notOpen){
                try {
                    close();
                }catch (Exception e){
                    log.error("Trying to close the blockstore after get a block because of service stopped", e);
                }
            }
        }
    }

    @Override
    public synchronized void setChainHead(StoredBlock chainHead) throws BlockStoreException {
        try {
            db.put(CHAIN_HEAD_KEY_STRING, chainHead.getHeader().getHash().getBytes());
        } catch (SnappydbException e) {
            log.error("cannot store chainhead ", e);
            throw new BlockStoreException(e);
        }
    }

    @Override
    public synchronized void close() throws BlockStoreException {
        try {
            db.close();
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

    public void truncate() throws Exception {
        db.destroy();
    }

    public synchronized void rollbackTo(int height) throws BlockStoreException {
        //lock.lock();
        try {
            StoredBlock block = getChainHead();
            List<Sha256Hash> blocksToRemove = new ArrayList<>();
            StoredBlock newChainHead = null;
            if (block.getHeight() <= height || height <= 0) throw new IllegalArgumentException("Invalid height");
            for (;;){
                blocksToRemove.add(block.getHeader().getHash());
                block = block.getPrev(this);
                if (block.getHeight() == height){
                    newChainHead = block;
                    break;
                }
            }
            for (int i = 0; i < height; i++) {
                blocksToRemove.add(block.getHeader().getHash());
                block = block.getPrev(this);
            }

            // Now remove every block
            for (Sha256Hash sha256Hash : blocksToRemove) {
                db.del(sha256Hash.toString());
            }

            setChainHead(newChainHead);
        } catch (SnappydbException e) {
            throw new BlockStoreException("Exception trying to remove values from the db" ,e);
        } finally {
            //lock.unlock();
        }
    }

    public synchronized void rollbackTo(Sha256Hash blockHash) throws BlockStoreException {
        //lock.lock();
        try {
            // First check if its exists..
            List<Sha256Hash> blocksToRemove = new ArrayList<>();
            StoredBlock block = getChainHead();
            StoredBlock chainHead;
            while (true) {
                if (block.getHeader().getHashAsString().equals(blockHash.toString())) {
                    chainHead = block;
                    break;
                }else {
                    blocksToRemove.add(block.getHeader().getHash());
                    block = block.getPrev(this);
                    if (block == null || block.equals(storedGenesis)){
                        throw new BlockStoreException("Block not found");
                    }
                }
            }

            // Now remove every block
            for (Sha256Hash sha256Hash : blocksToRemove) {
                db.del(sha256Hash.toString());
            }

            setChainHead(chainHead);
        } catch (SnappydbException e) {
            throw new BlockStoreException("Exception trying to remove values from the db" ,e);
        } finally {
            //lock.unlock();
        }
    }
}
