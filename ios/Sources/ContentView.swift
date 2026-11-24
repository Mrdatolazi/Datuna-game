import SwiftUI
import WebKit

struct ContentView: View {
    var body: some View {
        let base = DatunaWebView()
            .background(Color(red: 16/255, green: 20/255, blue: 28/255))
            .ignoresSafeArea()
            .statusBarHidden(true)

        if #available(iOS 16.0, *) {
            base.persistentSystemOverlays(.hidden)
        } else {
            base
        }
    }
}

struct DatunaWebView: UIViewRepresentable {
    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        config.allowsInlineMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []

        let webView = WKWebView(frame: .zero, configuration: config)
        webView.isOpaque = false
        webView.backgroundColor = UIColor(red: 16/255, green: 20/255, blue: 28/255, alpha: 1)
        webView.allowsLinkPreview = false

        let scroll = webView.scrollView
        scroll.isScrollEnabled = false
        scroll.bounces = false
        scroll.contentInsetAdjustmentBehavior = .never
        scroll.minimumZoomScale = 1.0
        scroll.maximumZoomScale = 1.0

        let url = Bundle.main.url(forResource: "index", withExtension: "html", subdirectory: "web")
            ?? Bundle.main.url(forResource: "index", withExtension: "html")

        if let fileURL = url {
            webView.loadFileURL(fileURL, allowingReadAccessTo: fileURL.deletingLastPathComponent())
        }

        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}
}
