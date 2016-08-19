//
//  SchedulerViewController.swift
//  zbthome
//
//  Created by Mackbook on 12/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class SchedulerViewController: UIViewController {
    
    
    @IBOutlet var deviceView: UIStackView!
    @IBOutlet var scrollView: UIScrollView!
    @IBOutlet var schedularNameText: UITextField!
    var roomName:String!
   
    var name:String = "";
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
        
        
        schedularNameText.text = name;
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
        let grpDao:SchedulerDAO = SchedulerDAO( name: schedularNameText.text!,deviceId: 1);
        DBOperation.getInstance().addScheduler(roomName, light: grpDao)
    }
    
    func updateDAO(scheduler: SchedulerDAO) {
        print("Update..........DAO  ....\(scheduler.name)")
        name = scheduler.name
    }
}
