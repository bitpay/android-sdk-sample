package com.bitpay.eordano.musicstore;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bitpay.eordano.musicstore.models.Invoice;
import com.bitpay.eordano.musicstore.models.Item;
import com.bitpay.sdk.android.BitPayAndroid;
import com.bitpay.sdk.android.InvoiceActivity;
import com.bitpay.sdk.controller.BitPay;
import com.bitpay.sdk.controller.BitPayException;
import com.bitpay.sdk.controller.KeyUtils;

import org.apache.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;


public class CartActivity extends Activity {

    private Invoice invoice;
    private ArrayList<String> data = new ArrayList<String>();
    private double total = 0.0;

    private void sendInvoice() {

        calculateValues();

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Holo_Dialog);
        dialog.setCancelable(false);
        dialog.setTitle("Creating invoice...");
        dialog.show();
        new BitPayAndroid.GetBitPayClientTask() {
            @Override
            protected void onPostExecute(final BitPayAndroid bitPayAndroid) {
                new BitPayAndroid.CreateInvoiceTask(bitPayAndroid) {
                    @Override
                    protected void onPostExecute(com.bitpay.sdk.model.Invoice invoice) {
                        if (invoice != null) {

                            Intent invoiceIntent = new Intent(CartActivity.this, InvoiceActivity.class);
                            invoiceIntent.putExtra("mInvoiceId", invoice.getId());
                            invoiceIntent.putExtra("mPaymentUri", invoice.getUrl());
                            try {
                                invoiceIntent.putExtra("mEcKey", KeyUtils.exportEcKey(bitPayAndroid.mBitpay._ecKey));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                }
                            });
                            startActivity(invoiceIntent);
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new com.bitpay.sdk.model.Invoice(total, "USD"));
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getString(R.string.privateKey));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        if (savedInstanceState != null) {
            invoice = savedInstanceState.getParcelable("invoice");
        } else {
            invoice = getIntent().getExtras().getParcelable("invoice");
        }
        calculateValues();
        ((ListView) findViewById(R.id.listView)).setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;

                if (null == convertView) {
                    row = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
                } else {
                    row = convertView;
                }

                TextView tv = (TextView) row.findViewById(android.R.id.text1);
                tv.setText(getItem(position));

                return row;
            }
        });
        ((TextView) findViewById(R.id.textView)).setText(String.format("Your total: %.2f", total));
        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendInvoice();
            }
        });
    }

    private void calculateValues() {
        data.clear();
        for (Item item : Invoice.ITEMS) {
            if (invoice.get(item) != 0) {
                double price = invoice.get(item) * item.price;
                data.add(String.format("%d x %s ($ %.2f)", invoice.get(item), item.name, price));
                total += price;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
