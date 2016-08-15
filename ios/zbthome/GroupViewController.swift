//
//  GroupViewController.swift
//  zbthome
//
//  Created by Mackbook on 12/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class GroupViewController: UIViewController {

    @IBOutlet var scrollView: UIScrollView!
    @IBOutlet var deviceView: UIStackView!
    @IBOutlet var groupNameText: UITextField!
    var roomName:String!
    let cellReuseIdentifier = "cell"
    override func viewDidLoad() {
        super.viewDidLoad()
        scrollView.translatesAutoresizingMaskIntoConstraints = false;
        //scrollView.addSubview(view)
        scrollView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[deviceView]|", options: NSLayoutFormatOptions(rawValue:0),metrics: nil, views: ["deviceView":deviceView]))
        scrollView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|[deviceView]|", options: NSLayoutFormatOptions(rawValue:0),metrics: nil, views: ["deviceView":deviceView]))
        scrollView.addConstraint(
            NSLayoutConstraint(item: scrollView,
                attribute: .Width,
                relatedBy: .Equal,
                toItem: deviceView,
                attribute: .Width,
                multiplier: 1.0,
                constant: 0))
        
        // Do any additional setup after loading the view, typically from a nib.
        var selectDeviceComp = SelectDeviceView(frame: deviceView.bounds, title:"Lights",tag:1);
        deviceView.addArrangedSubview(selectDeviceComp)
        var selectDeviceComp1 = SelectDeviceView(frame: deviceView.bounds, title:"Ra",tag:1);
        deviceView.addArrangedSubview(selectDeviceComp1)


    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    func setRoomDeviceName(name:String) {
        roomName = name
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("From GrpupViewController seqgueid...\(segue.identifier)")
        let grpDao:GroupDAO = GroupDAO( name: groupNameText.text!,deviceId: 1);
        DBOperation.getInstance().addGroup(roomName, light: grpDao)
    }
    
    

}
