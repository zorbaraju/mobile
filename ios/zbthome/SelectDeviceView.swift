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
    
    @IBOutlet var controllerStatusLabel: UILabel!
    @IBOutlet var checkBox: CheckBoxView!
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
            sliderDemo.maximumValue = 10
            sliderDemo.sizeToFit()
            sliderDemo.continuous = true
            controllerView.addSubview(sliderDemo)
            sliderDemo.autoresizingMask = [ .FlexibleTopMargin, .FlexibleBottomMargin]
            
            sliderDemo.addTarget(self, action: "sliderValueDidChange:", forControlEvents: .ValueChanged)
            sliderValueDidChange(sliderDemo)

        } else {
            switchView = UISwitch(frame: controllerView.frame);
            switchView.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]
            controllerView.addSubview(switchView)
            switchView.autoresizingMask = [ .FlexibleTopMargin, .FlexibleBottomMargin,
                                          .FlexibleLeftMargin, .FlexibleRightMargin ]
            switchView.addTarget(self, action: "switchValueDidChange:", forControlEvents: .ValueChanged);
            switchValueDidChange(switchView)
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

    func switchValueDidChange(sender:UISwitch!)
    {
        if (sender.on == true){
            controllerStatusLabel.text = "On"
        } else {
            controllerStatusLabel.text = "Off"
        }
    }
    func sliderValueDidChange(sender:UISlider!)
    {
        let newValue = Int(sender.value/1) * 1
        sender.setValue(Float(newValue), animated: false)
        var percValue = "0 %"
        if( newValue>0) {
            percValue = "\(newValue)0 %"
        }
        controllerStatusLabel.text = percValue
    }
    
    func selectComp(checked: Bool) {
        checkBox.isChecked = checked
    }
    
    func isCompSelected()->Bool {
        return checkBox.isChecked
    }
    
    func setControllerValue(controllerValue:Int) {
        if( switchView == nil) {
            sliderDemo.value = Float(controllerValue)
            sliderValueDidChange(sliderDemo)
        } else {
            switchView.on = (controllerValue != 0)
            switchValueDidChange(switchView)
        }
    }

    
    func getControllerValue()->Int {
        var controllerValue:Int = 0
        if( switchView == nil) {
            controllerValue = Int(sliderDemo.value)
        } else {
            if (switchView.on == true) {
                controllerValue = 9
            }
        }
        print("Selected vdecov comp.....getvalue.....\(controllerValue)");
        return controllerValue
    }
}
