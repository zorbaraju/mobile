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
        self.addTarget(self, action: "buttonClicked:", forControlEvents: UIControlEvents.TouchUpInside)
        self.isChecked = false
    }
    
    func buttonClicked(sender: UIButton) {
        if sender == self {
            isChecked = !isChecked
        }
    }

}
