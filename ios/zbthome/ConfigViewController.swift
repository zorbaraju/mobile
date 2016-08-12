//
//  ConfigViewController.swift
//  second
//
//  Created by Mackbook on 26/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class ConfigViewController: UIViewController {

    var collapseComp:FV!
    var roomDeviceName:String!
    var daoType:Int!

    @IBOutlet var nameText: UITextField!
    @IBOutlet var deviceIdText: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func setRoomDeviceName(collapseComp:FV, name:String, tag:Int) {
        self.collapseComp = collapseComp
        roomDeviceName = name
        daoType = tag
    }
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("prepareForSegue.....\(segue.identifier)"+roomDeviceName+"  daotype=\(daoType)")
        if( segue.identifier == "goBackFromSave") {
            if( daoType == 1) {
                let light:DeviceDAO = DeviceDAO(deviceName: nameText.text!, deviceId: Int(deviceIdText.text!)!);
                DBOperation().addLight(roomDeviceName, light: light)
            } else if( daoType == 2) {
                let light:DeviceDAO = DeviceDAO(deviceName: nameText.text!, deviceId: Int(deviceIdText.text!)!);
                DBOperation().addDevice(roomDeviceName, light: light)
            } else if( daoType == 3) {
                let light:DeviceDAO = DeviceDAO(deviceName: nameText.text!, deviceId: Int(deviceIdText.text!)!);
                DBOperation().addLight(roomDeviceName, light: light)
            }
            
        }
        
    }
    /*override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject!) -> Bool {
           print("preshouldpareForSegue.....\(identifier)")
        return  identifier == "goBackFromCancel"
    }*/
}
