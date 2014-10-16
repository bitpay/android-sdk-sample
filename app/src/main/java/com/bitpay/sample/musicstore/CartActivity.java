package com.bitpay.sample.musicstore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bitpay.sample.musicstore.models.MyInvoice;
import com.bitpay.sample.musicstore.models.Item;
import com.bitpay.sdk.android.BitPayAndroid;
import com.bitpay.sdk.android.InvoiceActivity;
import com.bitpay.sdk.android.interfaces.BitpayPromiseCallback;
import com.bitpay.sdk.android.interfaces.InvoicePromiseCallback;
import com.bitpay.sdk.controller.BitPayException;
import com.bitpay.sdk.model.Invoice;

import java.util.ArrayList;


public class CartActivity extends Activity {

    public static final String BITPAY_INVOICE = "BITPAY_INVOICE";
    public static final String MY_INVOICE= "MY_INVOICE";

    private static final int PAYMENT_RESULT = 12;

    private MyInvoice invoice;
    private ArrayList<String> data = new ArrayList<String>();
    private double total = 0.0;
    private Invoice bitpayInvoice;
    private BitPayAndroid bitpay;

    private void sendInvoice() {

        calculateValues();

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Holo_Dialog);
        dialog.setCancelable(false);
        dialog.setTitle("Creating invoice...");
        dialog.show();
        BitPayAndroid.withToken(getString(R.string.token), "https://test.bitpay.com/").then(new BitpayPromiseCallback() {

            @Override
            public void onSuccess(final BitPayAndroid bitpay) {
                CartActivity.this.bitpay = bitpay;
                bitpay.createNewInvoice(new com.bitpay.sdk.model.Invoice(total, "USD")).then(new InvoicePromiseCallback() {
                    @Override
                    public void onSuccess(com.bitpay.sdk.model.Invoice invoice) {

                        bitpayInvoice = invoice;
                        Intent invoiceIntent = new Intent(CartActivity.this, InvoiceActivity.class);
                        Log.d("Invoice", invoice.getPaymentUrls().getBIP21());
                        invoiceIntent.putExtra(InvoiceActivity.INVOICE, invoice);
                        invoiceIntent.putExtra(InvoiceActivity.CLIENT, bitpay);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                            }
                        });
                        startActivityForResult(invoiceIntent, PAYMENT_RESULT);
                    }

                    @Override
                    public void onError(BitPayException e) {
                        dialog.dismiss();
                        e.printStackTrace();

                        AlertDialog dialog = new AlertDialog.Builder(getApplicationContext()).setTitle("Creating Invoice").setMessage("Unable to create an invoice. Check your connection and token and try again.").create();
                        dialog.show();
                    }
                });
            }

            @Override
            public void onError(BitPayException e) {
                dialog.dismiss();
                e.printStackTrace();

                AlertDialog dialog = new AlertDialog.Builder(getApplicationContext()).setTitle("Creating BitPay Client").setMessage("Unable to connect to the server. Check your connection and token and try again.").create();
                dialog.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_RESULT) {
            if (resultCode == InvoiceActivity.RESULT_PAID) {
                Intent intent = new Intent(this, ReceiptActivity.class);
                intent.putExtra(ReceiptActivity.INVOICE, bitpayInvoice);
                intent.putExtra(ReceiptActivity.MY_INVOICE, invoice);
                startActivity(intent);
                finish();
            }
            if (resultCode == InvoiceActivity.RESULT_EXPIRED) {
                Toast.makeText(getApplicationContext(), "The payment window expired", Toast.LENGTH_LONG).show();
            }
            if (resultCode == InvoiceActivity.RESULT_USER_CANCELED) {
                Toast.makeText(getApplicationContext(), "The payment was canceled", Toast.LENGTH_LONG).show();
            }
            if (resultCode == InvoiceActivity.RESULT_OVERPAID) {
                Toast.makeText(getApplicationContext(), "The invoice was overpaid. Please contact us to request a refund.", Toast.LENGTH_LONG).show();
            }
            if (resultCode == InvoiceActivity.RESULT_PARTIALLY_PAID) {
                Toast.makeText(getApplicationContext(), "The invoice was paid only partially. Please finish paying it.", Toast.LENGTH_LONG).show();

                Intent invoiceIntent = new Intent(CartActivity.this, InvoiceActivity.class);
                invoiceIntent.putExtra(InvoiceActivity.INVOICE, bitpayInvoice);
                invoiceIntent.putExtra(InvoiceActivity.CLIENT, bitpay);
                startActivityForResult(invoiceIntent, PAYMENT_RESULT);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        if (savedInstanceState != null) {
            invoice = savedInstanceState.getParcelable(MY_INVOICE);
            bitpayInvoice = savedInstanceState.getParcelable(BITPAY_INVOICE);
        } else {
            invoice = getIntent().getExtras().getParcelable(MY_INVOICE);
            bitpayInvoice = getIntent().getExtras().getParcelable(BITPAY_INVOICE);
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
        total = 0.0;
        for (Item item : MyInvoice.ITEMS) {
            if (invoice.get(item) != 0) {
                double price = invoice.get(item) * item.price;
                data.add(String.format("%d x %s ($ %.2f)", invoice.get(item), item.name, price));
                total += price;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BITPAY_INVOICE, bitpayInvoice);
        outState.putParcelable(MY_INVOICE, invoice);
    }
}
