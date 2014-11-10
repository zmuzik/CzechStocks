package zmuzik.czechstocks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.List;

import zmuzik.czechstocks.R;
import zmuzik.czechstocks.dao.Stock;

public class AddStockAdapter extends ArrayAdapter<Stock> {

    public AddStockAdapter(Context context, int resource, List<Stock> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_edit_stock, parent, false);
        }

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
        checkBox.setText(getItem(position).getName());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getItem(position).setShowInQuotesList(b);
            }
        });
        return convertView;
    }
}