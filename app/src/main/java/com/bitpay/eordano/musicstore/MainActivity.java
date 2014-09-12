package com.bitpay.eordano.musicstore;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bitpay.eordano.musicstore.models.Invoice;
import com.bitpay.eordano.musicstore.models.Item;


public class MainActivity extends ListActivity {

    private Invoice invoice;

    public MainActivity() {
        this.invoice = new Invoice();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new BaseAdapter() {

            @Override
            public int getCount() {
                return Invoice.ITEMS.size();
            }

            @Override
            public Object getItem(int i) {
                return Invoice.ITEMS.get(i);
            }

            @Override
            public long getItemId(int i) {
                return R.layout.catalog_item;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.catalog_item, null);
                }
                final Item item = Invoice.ITEMS.get(position);
                ((TextView) convertView.findViewById(R.id.itemTitle)).setText(item.name + String.format(" ($ %.2f)", item.price));
                ((TextView) convertView.findViewById(R.id.itemAmount)).setText(
                        invoice.get(item) == 0 ? "None in the cart." : String.format("%d in the cart.", invoice.get(item)));
                ((Button) convertView.findViewById(R.id.addItem)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        invoice.addItem(item);
                        notifyDataSetChanged();
                    }
                });
                return convertView;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Open the cart activity with the current invoice as model
     */
    protected void openCart() {
        Intent intent = new Intent(this, CartActivity.class);
        Bundle b = new Bundle();
        b.putParcelable("invoice", invoice);
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_cart) {
            openCart();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
