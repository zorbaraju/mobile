//
//  GroupViewController.swift
//  zbthome
//
//  Created by Mackbook on 12/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class GroupViewController: UIViewController {

    @IBOutlet var groupNameText: UITextField!
    var roomName:String!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
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
        DBOperation().addGroup(roomName, light: grpDao)
    }

}
