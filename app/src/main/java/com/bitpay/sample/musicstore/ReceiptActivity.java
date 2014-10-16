package com.bitpay.sample.musicstore;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitpay.sample.musicstore.models.MyInvoice;
import com.bitpay.sample.musicstore.models.Item;
import com.bitpay.sdk.model.Invoice;

public class ReceiptActivity extends Activity {

    public static final String INVOICE = "invoice";
    public static final String MY_INVOICE = "myInvoice";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        LayoutInflater inflater = getLayoutInflater();
        MyInvoice invoice = getIntent().getExtras().getParcelable(MY_INVOICE);
        Invoice bitpayInvoice = getIntent().getExtras().getParcelable(INVOICE);

        TextView paragraph = (TextView) findViewById(R.id.textView2);
        paragraph.setText(String.format("We received your payment for %s BTC (%.2f USD). Your items will be arriving shortly.",
                bitpayInvoice.getBtcPrice(), bitpayInvoice.getPrice()));

        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout);
        for (Item item : MyInvoice.ITEMS) {
            if (invoice.get(item) > 0) {
                View newView = inflater.inflate(android.R.layout.simple_list_item_1, null);
                ((TextView) newView.findViewById(android.R.id.text1)).setText(invoice.get(item) + " x " + item.name);
                layout.addView(newView);
            }
        }
    }
}
