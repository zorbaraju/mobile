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
    
    var name:String = ""
    let dbOperation:DBOperation = DBOperation.getInstance()
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
        
        groupNameText.text = name;
        
        populateDevices();
    }
    
    func populateDevices() {
        let lights:[DeviceDAO] = dbOperation.getLights(roomName);
        print("lightcount......\(lights.count)")
        for var i = 0; i < lights.count; i += 1 {
            var selectDeviceComp = SelectDeviceView(frame: deviceView.bounds, deviceDAO: lights[i]);
            deviceView.addArrangedSubview(selectDeviceComp)
        }
        let devices:[DeviceDAO] = dbOperation.getDevices(roomName);
        for var i = 0; i < devices.count; i += 1 {
            var selectDeviceComp = SelectDeviceView(frame: deviceView.bounds, deviceDAO: devices[i]);
            deviceView.addArrangedSubview(selectDeviceComp)
        }
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
    
    func updateDAO(group: GroupDAO) {
        print("Update..........DAO  ....\(group.name)")
        name = group.name
    }

}
