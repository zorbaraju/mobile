//
//  HelpViewController.swift
//  second
//
//  Created by Mackbook on 28/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class HtmlViewController: UIViewController {
    var htmlFileName:String!
    var htmlTitle:String!
    @IBOutlet var titleView: UILabel!
    
    @IBOutlet var webview: UIWebView!
    override func viewDidLoad() {
        super.viewDidLoad()

        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: "goback")
        view.addGestureRecognizer(tap)
        let htmlFile = NSBundle.mainBundle().pathForResource(htmlFileName, ofType: "html")
        let html = try? String(contentsOfFile: htmlFile!, encoding: NSUTF8StringEncoding)
        titleView.text = htmlTitle
        webview.loadHTMLString(html!, baseURL: nil)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func goback() {
        self.dismissViewControllerAnimated(true, completion: nil)
    }

    func setHtmlFile(title:String, htmlfile:String) {
        self.htmlTitle = title;
        self.htmlFileName = htmlfile
    }
    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
