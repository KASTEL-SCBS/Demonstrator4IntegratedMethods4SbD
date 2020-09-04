package de.pcc.privacycrashcam.applicationlogic;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.RawRes;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.util.Scanner;

import de.pcc.privacycrashcam.R;

/**
 * Displays a dialog which shows an html file. Loading of the html file is done asynchronously.
 *
 * @author Giorgio Gross
 */
class HTMLDialogViewer {
    private Context context;
    private WebView wv_licenses;
    private ProgressBar pb_licenseLoader;
    private View layout;

    private String title;
    private
    @RawRes
    int rawRes;
    private String content;

    /**
     * Creates and shows a web view displaying the passed html file.
     *
     * @param context     application context
     * @param inflater    a layout inflater
     * @param title       the title of the dialog
     * @param rawResource the resource id of the html content
     */
    @SuppressLint("InflateParams")
    HTMLDialogViewer(Context context, LayoutInflater inflater, String title,
                     @RawRes int rawResource) {
        this.context = context;
        this.layout = inflater.inflate(R.layout.htmldialog, null); // pass null as we show a dialog
        this.title = title;
        this.content = "";
        this.rawRes = rawResource;
    }

    /**
     * Creates and shows a web view displaying the passed string inside an html file.
     *
     * @param context  application context
     * @param inflater a layout inflater
     * @param title    the title of the dialog
     * @param content  the content as a string to be shown
     */
    HTMLDialogViewer(Context context, LayoutInflater inflater, String title, String content) {
        this.context = context;
        this.layout = inflater.inflate(R.layout.htmldialog, null);
        this.title = title;
        this.content = content;
        this.rawRes = 0;
    }

    /**
     * Loads the specified raw resources and shows it in a dialog
     */
    void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        wv_licenses = (WebView) layout.findViewById(R.id.wv_licenses);
        pb_licenseLoader = (ProgressBar) layout.findViewById(R.id.pb_licenseLoader);

        new HTMLFileLoader().execute();

        builder.setTitle(title)
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).create().show();

    }

    /**
     * Async file loader for the html document.
     */
    private class HTMLFileLoader extends AsyncTask<Void, Void, Void> {
        StringBuilder sBuilder;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            sBuilder = new StringBuilder();

            if (rawRes != 0) {
                InputStream rawResource = context.getResources().openRawResource(rawRes);
                if (rawResource != null) {
                    Scanner scan = new Scanner(rawResource);

                    while (scan.hasNextLine()) {
                        sBuilder.append(scan.nextLine());
                        sBuilder.append("\n");
                    }
                }
            } else {
                sBuilder.append(content);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (wv_licenses != null && sBuilder != null && !isCancelled()) {
                wv_licenses.loadDataWithBaseURL(null, sBuilder.toString(), "text/html", "utf-8", null);
                wv_licenses.getSettings().setUseWideViewPort(true);
                pb_licenseLoader.setVisibility(View.GONE);
            }
        }
    }
}

