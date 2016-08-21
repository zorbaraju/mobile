//
//  CheckBoxViewController.swift
//  zbthome
//
//  Created by Mackbook on 14/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class CheckBoxView: UIButton {
    
    
    // Images
    let checkedImage = UIImage(named: "checked_ic_box")! as UIImage
    let uncheckedImage = UIImage(named: "unchecked_ic_box")! as UIImage
    
    // Bool property
    var isChecked: Bool = false {
        didSet{
            if isChecked == true {
                self.setImage(checkedImage, forState: .Normal)
            } else {
                self.setImage(uncheckedImage, forState: .Normal)
            }
        }
    }
    
    override func awakeFromNib() {
        self.addTarget(self, action: #selector(CheckBoxView.buttonClicked(_:)), forControlEvents: UIControlEvents.TouchUpInside)
        let lFrame = CGRectMake(0, 0, 44, 44);
        frame = lFrame
        self.isChecked = false
    }
    
    func performButtonClicked(target: AnyObject, callback: String) {
        self.addTarget(target, action: Selector(callback), forControlEvents: UIControlEvents.TouchUpInside)
    }
    
    func buttonClicked(sender: UIButton) {
        if sender == self {
            isChecked = !isChecked
        }
    }

}
