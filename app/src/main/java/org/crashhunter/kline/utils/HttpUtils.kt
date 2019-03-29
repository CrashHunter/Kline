//package org.crashhunter.kline.utils
//
//import com.gargoylesoftware.htmlunit.BrowserVersion
//import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
//import com.gargoylesoftware.htmlunit.WebClient
//import com.gargoylesoftware.htmlunit.html.HtmlPage
//import org.jsoup.Jsoup
//import org.jsoup.nodes.Document
//
//
///**
// * Created by CrashHunter on 2019/3/29.
// */
//class HttpUtils private constructor() {
//    /**
//     * 请求超时时间,默认20000ms
//     */
//    /**
//     * 设置请求超时时间
//     *
//     * @param timeout
//     */
//    var timeout = 20000
//    /**
//     * 等待异步JS执行时间,默认20000ms
//     */
//    /**
//     * 设置获取完整HTML页面时等待异步JS执行的时间
//     *
//     * @param waitForBackgroundJavaScript
//     */
//    var waitForBackgroundJavaScript = 20000
//
//    /**
//     * 获取页面文档字串(等待异步JS执行)
//     *
//     * @param url 页面URL
//     * @return
//     * @throws Exception
//     */
//    @Throws(Exception::class)
//    fun getHtmlPageResponse(url: String): String {
//        var result = ""
//
//        val webClient = WebClient(BrowserVersion.CHROME)
//
//        webClient.options.isThrowExceptionOnScriptError = false//当JS执行出错的时候是否抛出异常
//        webClient.options.isThrowExceptionOnFailingStatusCode = false//当HTTP的状态非200时是否抛出异常
//        webClient.options.isActiveXNative = false
//        webClient.options.isCssEnabled = false//是否启用CSS
//        webClient.options.isJavaScriptEnabled = true //很重要，启用JS
//        webClient.ajaxController = NicelyResynchronizingAjaxController()//很重要，设置支持AJAX
//
//        webClient.options.timeout = timeout//设置“浏览器”的请求超时时间
//        webClient.javaScriptTimeout = timeout.toLong()//设置JS执行的超时时间
//
//        val page: HtmlPage
//        try {
//            page = webClient.getPage(url)
//
//        } catch (e: Exception) {
//            webClient.close()
//            throw e
//        }
//
//        webClient.waitForBackgroundJavaScript(waitForBackgroundJavaScript.toLong())//该方法阻塞线程
//
//        result = page.asXml()
//        webClient.close()
//
//        return result
//    }
//
//    /**
//     * 获取页面文档Document对象(等待异步JS执行)
//     *
//     * @param url 页面URL
//     * @return
//     * @throws Exception
//     */
//    @Throws(Exception::class)
//    fun getHtmlPageResponseAsDocument(url: String): Document {
//        return parseHtmlToDoc(getHtmlPageResponse(url))
//    }
//
//    companion object {
//
//        private var httpUtils: HttpUtils? = null
//
//        /**
//         * 获取实例
//         *
//         * @return
//         */
//        val instance: HttpUtils
//            get() {
//                if (httpUtils == null)
//                    httpUtils = HttpUtils()
//                return httpUtils!!
//            }
//
//        /**
//         * 将网页返回为解析后的文档格式
//         *
//         * @param html
//         * @return
//         * @throws Exception
//         */
//        @Throws(Exception::class)
//        fun parseHtmlToDoc(html: String): Document {
//            return removeHtmlSpace(html)
//        }
//
//        private fun removeHtmlSpace(str: String): Document {
//            val doc = Jsoup.parse(str)
//            val result = doc.html().replace("&nbsp;", "")
//            return Jsoup.parse(result)
//        }
//    }
//}