//
//  CheckBoxViewController.swift
//  zbthome
//
//  Created by Mackbook on 14/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class SelectDeviceView: UIView {
    
    var deviceDAO:DeviceDAO!
    var view: UIView!
    
    @IBOutlet var controllerView: UIView!
    @IBOutlet var deviceNameText: UILabel!
    var switchView:UISwitch!
    var sliderDemo:UISlider!
    
    init(frame: CGRect,deviceDAO: DeviceDAO) {
        // 1. setup any properties here
        // 2. call super.init(frame:)
        super.init(frame: frame)
        self.deviceDAO = deviceDAO
        // 3. Setup view from .xib file
        xibSetup()
        print("init frame")
    }
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
        print("init coder")
    }
    
    func xibSetup() {
        
        view = loadViewFromNib()
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
        
        deviceNameText.text = deviceDAO.deviceName
        if( deviceDAO.isdimmable ) {
            sliderDemo = UISlider(frame:controllerView.frame)
            sliderDemo.minimumValue = 0
            sliderDemo.maximumValue = 9
            controllerView.addSubview(sliderDemo)
        } else {
            switchView = UISwitch(frame: controllerView.frame);
            switchView.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]
            controllerView.addSubview(switchView)
            switchView.autoresizingMask = [ .FlexibleTopMargin, .FlexibleBottomMargin,
                                          .FlexibleLeftMargin, .FlexibleRightMargin ]
        }
    }
    func loadViewFromNib() -> UIView {
        let bundle = NSBundle(forClass: self.dynamicType)
        let nib = UINib(nibName: "SelectDeviceView", bundle: bundle)
        let view = nib.instantiateWithOwner(self, options: nil)[0] as! UIView
        return view
    }
    
    
    override func layoutSubviews() {
        view.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]

    }
    }
