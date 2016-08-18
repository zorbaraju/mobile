//
//  ConfigViewController.swift
//  second
//
//  Created by Mackbook on 26/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class ConfigViewController: MenuViewController {

    let dbOperation: DBOperation = DBOperation.getInstance();
    @IBOutlet var deviceIdMenu: MenuUIView!
    @IBOutlet var titleLabel: UILabel!
    @IBOutlet var deviceTypeIconMenu: IconMenu!
    @IBOutlet var dimmableBox: CheckBoxView!
    var collapseComp:CollapseView!
    var roomDeviceName:String!
    var daoType:Int!

    var deviceIdMenuNames:[[String]] = [[String]]()
    
    var lightTypeMenuNames:[[String]] = [
        ["Light","light_off.png"],
        ["Tube Light","fan_off.png"]
    ]
    
    var dimmableLightTypeMenuNames:[[String]] = [
        ["Led","led_off.png"]
    ]
    
    var deviceTypeMenuNames:[[String]] = [
        ["Motor","light_off.png"],
        ["Computer","fan_off.png"]
    ]
    
    var dimmableDeviceTypeMenuNames:[[String]] = [
        ["Ac","light_off.png"],
        ["Fan","fan_off.png"]
    ]
    
    @IBOutlet var nameText: UITextField!
    @IBOutlet var deviceIdText: UITextField!
    
    var name:String = "";
    
    override func viewDidLoad() {
        super.viewDidLoad()

        deviceIdMenu.setParentView1(self, p: view,menuItemNames: deviceIdMenuNames);
        deviceTypeIconMenu.setParentView1(self, p: view, menuItemNames: lightTypeMenuNames);
        dimmableBox.performButtonClicked(self, callback: "dimmableClicked:")
        // Do any additional setup after loading the view.
        
        print("viewDidLoad")
        nameText.text = name;
        if( daoType == 1) {
            titleLabel.text = "Adding a Light"
        } else if( daoType == 2) {
            titleLabel.text = "Adding a Device"
        } else if( daoType == 201) {
            titleLabel.text = "Editing Light " + name
        } else if( daoType == 202) {
            titleLabel.text = "Editing Device " + name
        }
    }

    func dimmableClicked(sender: CheckBoxView) {
        print("dimmable clicked")
        var isDimmable = sender.isChecked;
         print("dimmable clicked isdimmable...\(isDimmable) \(daoType)" )
        if( daoType == 1) { // this is for light
            if( isDimmable) {
                deviceTypeIconMenu.setMenuItems(dimmableLightTypeMenuNames);
            } else {
                 deviceTypeIconMenu.setMenuItems(lightTypeMenuNames);
            }
        } else {
            if( isDimmable) {
                deviceTypeIconMenu.setMenuItems(dimmableDeviceTypeMenuNames);
            } else {
                deviceTypeIconMenu.setMenuItems(deviceTypeMenuNames);
            }
        }
        
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func setRoomDeviceName(collapseComp:CollapseView, name:String, tag:Int) {
        self.collapseComp = collapseComp
        roomDeviceName = name
        daoType = tag
        let roomDao:RoomDAO = dbOperation.getLastSelectedRoom();
        var dictionary = Dictionary<Int, Int>()
        let numDevices = roomDao.numDevices
        for (var i: Int = 1; i <= numDevices; i++) {
            dictionary[i] = i
        }
        let lights:[DeviceDAO] = dbOperation.getLights(roomDeviceName);
        for (var i: Int = 0; i < lights.count; i++) {
            dictionary.removeValueForKey(lights[i].deviceId)
        }
        let devices:[DeviceDAO] = dbOperation.getDevices(roomDeviceName);
        for (var i: Int = 0; i < devices.count; i++) {
            dictionary.removeValueForKey(devices[i].deviceId)
        }

        deviceIdMenuNames = [[String]]()
        for (var key: Int = 1; key <= numDevices; key++) {
            let keyExists = dictionary[key] != nil
            if( keyExists ) {
                deviceIdMenuNames.append([String(key),""])
            }
        }
    }
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject?) -> Bool {
        if( identifier != "goBackFromSave") {
            return true
        }
        var name = nameText.text
        print("Name from configname...\(name)")
        if( (name == "") ) {
            let alert = UIAlertView();
            alert.title = "Title"
            alert.message = "Device name is empty"
            alert.addButtonWithTitle("Ok")
            alert.show()
            return false
        }
        var deviceId = deviceIdMenu.getSelectedText()
        print("deviceId from configname...\(deviceId)")
        if( (deviceId == "") ) {
            let alert = UIAlertView();
            alert.title = "Title"
            alert.message = "Device id is empty"
            alert.addButtonWithTitle("Ok")
            alert.show()
            return false
        }
        return true
    }
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("prepareForSegue.....\(segue.identifier)"+roomDeviceName+"  daotype=\(daoType)")
        if( segue.identifier == "goBackFromSave") {
            var name = nameText.text
            var deviceId = deviceIdMenu.getSelectedText()
            if( daoType == 1) {
                let light:DeviceDAO = DeviceDAO(deviceName: name!, deviceId: Int(deviceId)!);
                dbOperation.addLight(roomDeviceName, light: light)
            } else if( daoType == 2) {
                let light:DeviceDAO = DeviceDAO(deviceName: name!, deviceId: Int(deviceId)!);
                dbOperation.addDevice(roomDeviceName, light: light)
           }
            
        }
        
    }
    
    override func menuItemClicked(sourceMenu:UIView, rowIndex: Int) {
    }
    
    func updateDAO(device: DeviceDAO) {
        print("Update..........DAO  ....\(device.deviceName)")
        self.name = device.deviceName
    }
}
