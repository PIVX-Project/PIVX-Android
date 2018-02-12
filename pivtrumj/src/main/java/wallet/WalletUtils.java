package wallet;

import org.apache.commons.codec.Charsets;
import org.pivxj.core.Address;
import org.pivxj.core.AddressFormatException;
import org.pivxj.core.Coin;
import org.pivxj.core.DumpedPrivateKey;
import org.pivxj.core.ECKey;
import org.pivxj.core.InsufficientMoneyException;
import org.pivxj.core.NetworkParameters;
import org.pivxj.core.ScriptException;
import org.pivxj.core.Sha256Hash;
import org.pivxj.core.Transaction;
import org.pivxj.core.TransactionInput;
import org.pivxj.core.TransactionOutPoint;
import org.pivxj.core.TransactionOutput;
import org.pivxj.script.Script;
import org.pivxj.wallet.DefaultCoinSelector;
import org.pivxj.wallet.KeyChainGroup;
import org.pivxj.wallet.UnreadableWalletException;
import org.pivxj.wallet.Wallet;
import org.pivxj.wallet.WalletProtobufSerializer;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import global.utils.Iso8601Format;

public class WalletUtils
{

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("WalletUtils");

	public static long longHash(final Sha256Hash hash) {
		final byte[] bytes = hash.getBytes();

		return (bytes[31] & 0xFFl) | ((bytes[30] & 0xFFl) << 8) | ((bytes[29] & 0xFFl) << 16) | ((bytes[28] & 0xFFl) << 24)
				| ((bytes[27] & 0xFFl) << 32) | ((bytes[26] & 0xFFl) << 40) | ((bytes[25] & 0xFFl) << 48) | ((bytes[23] & 0xFFl) << 56);
	}


	public static Address getToAddressOfSent(NetworkParameters params,final Transaction tx, final Wallet wallet)
	{
		for (final TransactionOutput output : tx.getOutputs())
		{
			try
			{
				if (!output.isMine(wallet))
				{
					final Script script = output.getScriptPubKey();
					return script.getToAddress(params, true);
				}
			}
			catch (final ScriptException x)
			{
				// swallow
			}
		}

		return null;
	}

	public static Address getWalletAddressOfReceived(NetworkParameters params,final Transaction tx, final Wallet wallet)
	{
		for (final TransactionOutput output : tx.getOutputs())
		{
			try
			{
				if (output.isMine(wallet))
				{
					final Script script = output.getScriptPubKey();
					return script.getToAddress(params, true);
				}
			}
			catch (final ScriptException x)
			{
				// swallow
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}

		return null;
	}

	public static boolean isEntirelySelf(final Transaction tx, final Wallet wallet)
	{
		for (final TransactionInput input : tx.getInputs())
		{
			final TransactionOutput connectedOutput = input.getConnectedOutput();
			if (connectedOutput == null || !connectedOutput.isMine(wallet))
				return false;
		}

		for (final TransactionOutput output : tx.getOutputs())
		{
			if (!output.isMine(wallet))
				return false;
		}

		return true;
	}

	public static Wallet restoreWalletFromProtobufOrBase58(final InputStream is, final NetworkParameters expectedNetworkParameters,long backupMaxChars) throws IOException
	{
		is.mark((int) backupMaxChars);

		try
		{
			return restoreWalletFromProtobuf(is, expectedNetworkParameters);
		}
		catch (final IOException x)
		{
			try
			{
				is.reset();
				return restorePrivateKeysFromBase58(is, expectedNetworkParameters,backupMaxChars);
			}
			catch (final IOException x2)
			{
				throw new IOException("cannot read protobuf (" + x.getMessage() + ") or base58 (" + x2.getMessage() + ")", x);
			}
		}
	}

	public static Wallet restoreWalletFromProtobuf(final InputStream is, final NetworkParameters expectedNetworkParameters) throws IOException {
		try {
			final Wallet wallet = new WalletProtobufSerializer().readWallet(is, true, null);
			if (!wallet.getParams().equals(expectedNetworkParameters))
				throw new IOException("bad wallet backup network parameters: " + wallet.getParams().getId());
			if (!wallet.isConsistent())
				throw new IOException("inconsistent wallet backup");

			return wallet;
		} catch (final UnreadableWalletException x) {
			throw new IOException("unreadable wallet", x);
		}
	}

	public static Wallet restorePrivateKeysFromBase58(final InputStream is, final NetworkParameters expectedNetworkParameters,long backupMaxChars) throws IOException
	{
		final BufferedReader keyReader = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));

		// create non-HD wallet
		final KeyChainGroup group = new KeyChainGroup(expectedNetworkParameters);
		group.importKeys(WalletUtils.readKeys(keyReader, expectedNetworkParameters,backupMaxChars));
		return new Wallet(expectedNetworkParameters, group);
	}

	public static void writeKeys(final NetworkParameters params,final Writer out, final List<ECKey> keys) throws IOException
	{
		final DateFormat format = Iso8601Format.newDateTimeFormatT();

		out.write("# KEEP YOUR PRIVATE KEYS SAFE! Anyone who can read this can spend your IoPs.\n");

		for (final ECKey key : keys)
		{
			out.write(key.getPrivateKeyEncoded(params).toBase58());
			if (key.getCreationTimeSeconds() != 0)
			{
				out.write(' ');
				out.write(format.format(new Date(key.getCreationTimeSeconds() * TimeUnit.SECONDS.toMillis(1))));//DateUtils.SECOND_IN_MILLIS
			}
			out.write('\n');
		}
	}

	public static List<ECKey> readKeys(final BufferedReader in, final NetworkParameters expectedNetworkParameters,long backupMaxChars) throws IOException
	{
		try
		{
			final DateFormat format = Iso8601Format.newDateTimeFormatT();

			final List<ECKey> keys = new LinkedList<ECKey>();

			long charCount = 0;
			while (true)
			{
				final String line = in.readLine();
				if (line == null)
					break; // eof
				charCount += line.length();
				if (charCount > backupMaxChars)
					throw new IOException("read more than the limit of " + backupMaxChars + " characters");
				if (line.trim().isEmpty() || line.charAt(0) == '#')
					continue; // skip comment

				final String[] parts = line.split(" ");

				final ECKey key = DumpedPrivateKey.fromBase58(expectedNetworkParameters, parts[0]).getKey();
				key.setCreationTimeSeconds(parts.length >= 2 ? format.parse(parts[1]).getTime() / TimeUnit.SECONDS.toMillis(1) : 0);  //DateUtils.SECOND_IN_MILLIS

				keys.add(key);
			}

			return keys;
		}
		catch (final AddressFormatException x)
		{
			throw new IOException("cannot read keys", x);
		}
		catch (final ParseException x)
		{
			throw new IOException("cannot read keys", x);
		}
	}

	public static final FileFilter BACKUP_FILE_FILTER = new FileFilter()
	{
		@Override
		public boolean accept(final File file)
		{
			InputStream is = null;

			try {
				if (file==null)return false;
				is = new FileInputStream(file);
				return WalletProtobufSerializer.isWallet(is);
			}
			catch (final IOException x)
			{
				return false;
			}
			finally
			{
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (final IOException x)
					{
						// swallow
					}
				}
			}
		}
	};

	public static byte[] walletToByteArray(final Wallet wallet)
	{
		try
		{
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			new WalletProtobufSerializer().writeWallet(wallet, os);
			os.close();
			return os.toByteArray();
		}
		catch (final IOException x)
		{
			throw new RuntimeException(x);
		}
	}

	public static Wallet walletFromByteArray(final byte[] walletBytes)
	{
		try
		{
			final ByteArrayInputStream is = new ByteArrayInputStream(walletBytes);
			final Wallet wallet = new WalletProtobufSerializer().readWallet(is);
			is.close();
			return wallet;
		}
		catch (final UnreadableWalletException x)
		{
			throw new RuntimeException(x);
		}
		catch (final IOException x)
		{
			throw new RuntimeException(x);
		}
	}

	public static boolean isPayToManyTransaction(final Transaction transaction)
	{
		return transaction.getOutputs().size() > 20;
	}

//	public static boolean isPayToManyTransaction(final TransactionCacheData transaction)
//	{
//		return transaction.getOutputsSize() > 20;
//	}

	/**
	 * Return a list of unspent transaction satisfasing the total param amount
	 *
	 * @param totalAmount
	 * @return
	 */
	public static List<TransactionOutput> getInputsForAmount(Wallet wallet,Coin totalAmount,List<TransactionOutput> unspent,List<TransactionOutput> usedOutputs,OutputsLockedListener outputsLockedListener) throws InsufficientMoneyException {
		List<TransactionOutput> unspentTransactions = new ArrayList<>();
		Coin totalInputsValue = Coin.ZERO;
		boolean inputsSatisfiedContractValue = false;

		for (TransactionOutput transactionOutput : wallet.getUnspents()) {
			//
			TransactionOutPoint transactionOutPoint = transactionOutput.getOutPointFor();
			if (outputsLockedListener.isOutputLocked(transactionOutPoint.getHash().toString(), transactionOutPoint.getIndex())) {
				continue;
			}
			if (usedOutputs!=null && usedOutputs.contains(transactionOutput)){
				LOG.info("Output already used");
				continue;
			}

			if (DefaultCoinSelector.isSelectable(transactionOutput.getParentTransaction()) && transactionOutput.isAvailableForSpending() && transactionOutput.getParentTransaction().isMature()) {
				LOG.info("adding non locked transaction to spend as an input: postion:" + transactionOutPoint.getIndex() + ", parent hash: " + transactionOutPoint.toString());
				totalInputsValue = totalInputsValue.add(transactionOutput.getValue());
				unspentTransactions.add(transactionOutput);
				if (totalInputsValue.isGreaterThan(totalAmount)) {
					inputsSatisfiedContractValue = true;
					break;
				}
			}
		}

		if (!inputsSatisfiedContractValue)
			throw new InsufficientMoneyException(totalInputsValue,"Inputs not satisfied vote value");

		return unspentTransactions;
	}


	public interface OutputsLockedListener {
		boolean isOutputLocked(String hash, long index);
	}


	/**
	 * @param list
	 * @return
	 */
	public static Coin sumValue(List<TransactionOutput> list){
		Coin ret = Coin.ZERO;
		for (TransactionOutput transactionOutput : list) {
			ret = ret.add(transactionOutput.getValue());
		}
		return ret;
	}


	public static List<TransactionOutput> sortOutputsHighToLowValue(List<TransactionOutput> unspents) {
		Collections.sort(unspents, new Comparator<TransactionOutput>() {
			@Override
			public int compare(TransactionOutput z1, TransactionOutput z2) {
				if (z1.getValue().isGreaterThan(z2.getValue()))
					return -1;
				if (z1.getValue().isLessThan(z2.getValue()))
					return 1;
				return 0;
			}
		});
		return unspents;
	}

	public static List<TransactionOutput> sortOutputsLowToHigValue(List<TransactionOutput> unspents) {
		Collections.sort(unspents, new Comparator<TransactionOutput>() {
			@Override
			public int compare(TransactionOutput z1, TransactionOutput z2) {
				if (z1.getValue().isGreaterThan(z2.getValue()))
					return 1;
				if (z1.getValue().isLessThan(z2.getValue()))
					return -1;
				return 0;
			}
		});
		return unspents;
	}
}
