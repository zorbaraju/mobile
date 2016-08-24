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
    @IBOutlet var devicesView: UIStackView!
    @IBOutlet var groupNameText: UITextField!
    var roomName:String!
    let cellReuseIdentifier = "cell"
    
    @IBOutlet var groupTitleLabel: UILabel!
    var currentGroup:GroupDAO!
    var indexAt:Int = -1
    
    let dbOperation:DBOperation = DBOperation.getInstance()
   
    override func viewDidLoad() {
        super.viewDidLoad()
        scrollView.translatesAutoresizingMaskIntoConstraints = false;
        //scrollView.addSubview(view)
        scrollView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[devicesView]|", options: NSLayoutFormatOptions(rawValue:0),metrics: nil, views: ["devicesView":devicesView]))
        scrollView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|[devicesView]|", options: NSLayoutFormatOptions(rawValue:0),metrics: nil, views: ["devicesView":devicesView]))
        scrollView.addConstraint(
            NSLayoutConstraint(item: scrollView,
                attribute: .Width,
                relatedBy: .Equal,
                toItem: devicesView,
                attribute: .Width,
                multiplier: 1.0,
                constant: 0))
        
        populateDevices();
        
    }
    
    func populateDevices() {
        var dictionary = Dictionary<Int, SelectDeviceView>()
        let lights:[DeviceDAO] = dbOperation.getLights(roomName);
        print("lightcount......\(lights.count)")
        for i in 0 ..< lights.count {
            let selectDeviceComp = SelectDeviceView(frame: devicesView.bounds, deviceDAO: lights[i]);
            devicesView.addArrangedSubview(selectDeviceComp)
            dictionary[lights[i].deviceId] = selectDeviceComp
        }
        let devices:[DeviceDAO] = dbOperation.getDevices(roomName);
        for i in 0 ..< devices.count {
            let selectDeviceComp = SelectDeviceView(frame: devicesView.bounds, deviceDAO: devices[i]);
            devicesView.addArrangedSubview(selectDeviceComp)
            dictionary[devices[i].deviceId] = selectDeviceComp
        }
        groupTitleLabel.text = "Adding a new group";
        if( currentGroup != nil) {
            
            groupTitleLabel.text = "Updating \(currentGroup.name)";
            groupNameText.text = currentGroup.name;
            let devInfo:[[Int]] = currentGroup.devicesArray
            print(" count......\(devInfo.count)")
            for i in 0 ..< devInfo.count {
                let devId = devInfo[i][0]
                let controllerValue = devInfo[i][1];
                let selectComp = dictionary[devId]
                print(" devid.........\(devId)...value...\(controllerValue)")

                selectComp?.selectComp(true)
                selectComp?.setControllerValue(controllerValue)
            }
        }
        
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    
    func setRoomDeviceName(name:String) {
        roomName = name
    }
    
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject?) -> Bool {
        if( identifier != "saveSegueId") {
            return true
        }
        let name = groupNameText.text
        print("Name from configname...\(name)")
        if( (name == "") ) {
            dbOperation.showAlert(self, message: "Group name is empty")
            return false
        }
        return true
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("From GrpupViewController seqgueid...\(segue.identifier)")
        if( segue.identifier == "saveSegueId") {
            let grpDao:GroupDAO = GroupDAO( name: groupNameText.text!);
            
            let numComps = devicesView.arrangedSubviews.count
            for i in 0 ..< numComps {
                let selComp = devicesView.arrangedSubviews[i] as! SelectDeviceView
                if ( selComp.isCompSelected() ) {
                    let devInfo:[Int] = [selComp.deviceDAO.deviceId, selComp.getControllerValue()]
                    grpDao.devicesArray.append(devInfo)
                    print(" devid.....\(selComp.deviceDAO.deviceId) and cvalue...\(selComp.getControllerValue())")
                }
                
            }
            
            if( currentGroup == nil) {
                dbOperation.addGroup(roomName, light: grpDao)
            } else {
                dbOperation.updateGroup(roomName, group: grpDao, indexAt: indexAt)
            }
        }
    }
    
    func updateDAO(group: GroupDAO, indexAt:Int) {
      //  print("Update..........DAO  ....\(group.name)")
        currentGroup = group
        self.indexAt = indexAt
    }

}
