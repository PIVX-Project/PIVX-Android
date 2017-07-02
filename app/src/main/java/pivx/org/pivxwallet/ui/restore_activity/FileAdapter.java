package pivx.org.pivxwallet.ui.restore_activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import pivx.org.pivxwallet.R;

public abstract class FileAdapter extends ArrayAdapter<File>
{
	protected final Context context;
	protected final LayoutInflater inflater;

	public FileAdapter(final Context context)
	{
		super(context, 0);

		this.context = context;
		inflater = LayoutInflater.from(context);
	}

	public void setFiles(final List<File> files)
	{
		clear();
		for (final File file : files)
			add(file);
	}

	@Override
	public View getView(final int position, View row, final ViewGroup parent)
	{
		final File file = getItem(position);

		if (row == null)
			row = inflater.inflate(R.layout.spinner_item, null);

		final TextView textView = (TextView) row.findViewById(android.R.id.text1);
		textView.setText(file.getName());

		return row;
	}
}