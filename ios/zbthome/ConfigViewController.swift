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
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func setRoomDeviceName(collapseComp:FV, name:String) {
        self.collapseComp = collapseComp
        roomDeviceName = name
    }
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("prepareForSegue.....\(segue.identifier)"+roomDeviceName)
        if( segue.identifier == "goBackFromSave") {
            let light:DeviceDAO = DeviceDAO(deviceName: "Name",deviceId: 1);
            DBOperation().addLight(roomDeviceName, light: light)
        }
        
    }
    /*override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject!) -> Bool {
           print("preshouldpareForSegue.....\(identifier)")
        return  identifier == "goBackFromCancel"
    }*/
}
