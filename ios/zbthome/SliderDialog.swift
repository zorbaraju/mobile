//
//  SliderDialog.swift
//  zbthome
//
//  Created by Mackbook on 03/09/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class SliderDialog: UIView {

    var delegate:ViewController;
    var sliderMethod:String;
    var value:Int!
    @IBOutlet var slider: UISlider!
   
    @IBAction func okButtonClicked(sender: UIButton) {
        print("Clse view")
        removeFromSuperview()
    }
    
    var view: UIView!
    
    init(frame: CGRect, delegate:ViewController, sliderMethod:String, value:Int) {
        self.delegate = delegate
        self.sliderMethod = sliderMethod
        self.value = value
        // 2. call super.init(frame:)
        super.init(frame: frame)
        
        // 3. Setup view from .xib file
        xibSetup()
        print("init frame")
    }
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        self.delegate = ViewController();
        self.slider = UISlider()
        self.sliderMethod = ""
        self.value = 0;
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
        print("init coder")
    }
    
    func xibSetup() {
        
        let bundle = NSBundle(forClass: self.dynamicType)
        let nib = UINib(nibName: "SliderDialog", bundle: bundle)
        view = nib.instantiateWithOwner(self, options: nil)[0] as! UIView
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
        
        //slider.tag = lightDAO.deviceId
        slider.minimumValue = 0
        slider.maximumValue = 10
        slider.sizeToFit()
        slider.continuous = true
        slider.value = Float(value)
        slider.addTarget(delegate, action: Selector(sliderMethod), forControlEvents: .ValueChanged)
       
    }
    
}
