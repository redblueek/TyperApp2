package ovh.grosik.typer;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private View rootView;
    private int originalBottomMargin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottom = 0;
            if(systemBars.bottom > 10) {
                bottom = systemBars.bottom-10;
            }
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });
        rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        try {
            WebView webView = findViewById(R.id.webView);
            Button button = findViewById(R.id.keyboard); // Replace with your Button ID
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setSafeBrowsingEnabled(true);
            webSettings.setDomStorageEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                @SuppressWarnings("deprecation")
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, String url)
                {
                    return shouldOverrideUrlLoading(url, webView);
                }

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request)
                {
                    Uri uri = request.getUrl();

                    return shouldOverrideUrlLoading(uri.toString(), webView);
                }

                private boolean shouldOverrideUrlLoading(final String url, WebView v) {

                    // Here put your code
                    if(!url.startsWith("https://")) {
                        try {
                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            if (intent != null) {
                                PackageManager packageManager = getPackageManager();
                                if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                                    startActivity(intent);
                                    return true;
                                } else {
                                    // App not installed, handle fallback URL
                                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                                    if (fallbackUrl != null) {
                                        v.loadUrl(fallbackUrl);
                                        return true;
                                    }
                                }
                            }
                        } catch (URISyntaxException e) {
                            // Handle parsing error
                        }
                    }
                    v.loadUrl(url);
                    return true; // Returning True means that application wants to leave the current WebView and handle the url itself, otherwise return false.
                }
            });

            webView.loadUrl("https://grosik.ovh/typer/");
            webView.requestFocus();
            View.OnClickListener kb = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.showSoftInput(webView, InputMethodManager.SHOW_IMPLICIT); // Use button as anchor

                    webView.requestFocus();

                }
            };
            button.setOnClickListener(kb);
            findViewById(R.id.cardView).setOnClickListener(kb);
            findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'F5'}))", null);
                    webView.requestFocus();
                }
            });
            findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'Control'}))", null);
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'l'}))", null);
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keyup',{key:'l'}))", null);
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keyup',{key:'Control'}))", null);
                    webView.requestFocus();
                }
            });
            final boolean[] control = {false};
            findViewById(R.id.ctrl).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    control[0] = !control[0];
                    if(control[0]) {
                        webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'Control'}))", null);
                    } else {
                        webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keyup',{key:'Control'}))", null);
                    }
                    ((Button)findViewById(R.id.ctrl)).setTextColor(control[0] ? getResources().getColor(R.color.buttons) : getResources().getColor(R.color.foreground));
                    webView.requestFocus();
                }
            });

            findViewById(R.id.arrowUp).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowUp'}))", null);
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keyup',{key:'ArrowUp'}))", null);
                }
            });
            findViewById(R.id.arrowDown).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowDown'}))", null);
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keyup',{key:'ArrowDown'}))", null);
                }
            });
            findViewById(R.id.arrowLeft).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowLeft'}))", null);
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keyup',{key:'ArrowLeft'}))", null);
                }
            });
            findViewById(R.id.arrowRight).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'ArrowRight'}))", null);
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keyup',{key:'ArrowRight'}))", null);
                }
            });
            findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.loadUrl("https://grosik.ovh/typer/");
                }
            });
            findViewById(R.id.paste).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard.hasPrimaryClip() && (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) || clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML))) {
                        ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                        String pasteData = item.getText().toString();
                        webView.evaluateJavascript("let event = new Event('paste'); event.clipboardData = {getData: function (format) {return '" + pasteData + "'}}; document.body.dispatchEvent(event);", null);
                    }
                }
            });
            findViewById(R.id.tab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keydown',{key:'Tab'}))", null);
                }
            });
            findViewById(R.id.slash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.evaluateJavascript("document.body.dispatchEvent(new KeyboardEvent('keypress',{key:'/'}))", null);
                }
            });
        } catch (Exception e) {};


    }



    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Rect rect = new Rect();
                rootView.getWindowVisibleDisplayFrame(rect);
                int screenHeight = rootView.getRootView().getHeight();
                int keyboardHeight = screenHeight - rect.bottom;
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rootView.getLayoutParams();

                if (keyboardHeight > 0) {
                    // Keyboard is shown
                    originalBottomMargin = params.bottomMargin;
                    params.bottomMargin = keyboardHeight;
                } else {
                    // Keyboard is hidden
                    params.bottomMargin = originalBottomMargin;
                }

                rootView.setLayoutParams(params);
                rootView.invalidate();
            } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                rootView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @NonNull
                    @Override
                    public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets insets) {
                        int keyboardHeight = insets.getInsets(WindowInsets.Type.ime()).bottom;
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rootView.getLayoutParams();
                        if (keyboardHeight > 0) {
                            params.bottomMargin = keyboardHeight;
                        } else {
                            params.bottomMargin = originalBottomMargin;
                        }
                        rootView.setLayoutParams(params);
                        return insets;
                    }
                });
            }
        }
    };
}