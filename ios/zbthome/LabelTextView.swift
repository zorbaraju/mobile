//
//  ZIconCell.swift
//  second
//
//  Created by Mackbook on 31/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class LabelTextView: UIView {
    var labelName:String!
    
    @IBOutlet var inputText: UITextField!
    @IBOutlet var titleLabel: UILabel!
    var view: UIView!
    
    override init(frame: CGRect) {
        // 1. setup any properties here
        
        // 2. call super.init(frame:)
        super.init(frame: frame)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    
    init(frame: CGRect,title: String) {
        super.init(frame: frame)
        xibSetup()
        self.labelName = title
        titleLabel.text = labelName
    }
    
    func setLabel(title: String) {
        self.labelName = title
        titleLabel.text = labelName
    }
    
    func getValue()->String{
        return inputText.text!
    }
    
    func xibSetup() {
        view = loadViewFromNib()
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
    }
    
    func loadViewFromNib() -> UIView {
        
        let bundle = NSBundle(forClass: self.dynamicType)
        let nib = UINib(nibName: "LabelTextView", bundle: bundle)
        let view = nib.instantiateWithOwner(self, options: nil)[0] as! UIView
        return view
    }
}
