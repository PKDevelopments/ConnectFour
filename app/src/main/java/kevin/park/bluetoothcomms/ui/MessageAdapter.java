package kevin.park.bluetoothcomms.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import kevin.park.bluetoothcomms.R;

public class MessageAdapter extends BaseAdapter {
    public Context mContext;
    public List<String> messages;

    public MessageAdapter(Context context, List<String> inputs){
        mContext = context;
        messages = inputs;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
        }
        TextView textView = convertView.findViewById(R.id.message_view);
        textView.setText((String)getItem(position)+"");

        return convertView;
    }
}
