//
//  SchedularViewController.swift
//  zbthome
//
//  Created by Mackbook on 12/08/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class SchedulerViewController: MenuViewController {
    
    @IBOutlet var scrollView: UIScrollView!
    @IBOutlet var deviceView: UIStackView!
    
    @IBOutlet var day1box: CheckBoxView!
    @IBOutlet var day2box: CheckBoxView!
    @IBOutlet var startTimeText: UITextField!
    @IBOutlet var schedularNameText: UITextField!
    
    @IBOutlet var repeatType: MenuUIView!
    @IBOutlet var quickDaysView: UIStackView!
    var roomName:String!
    var types:String!

    let cellReuseIdentifier = "cell"
    
    var daysBox:[CheckBoxView] = [CheckBoxView]()
    
    @IBOutlet var schedularTitleLabel: UILabel!
    var currentScheduler:SchedulerDAO!
    var indexAt:Int = -1
    
    var repeatTypeMenuNames:[[String]] = [
        ["once","light_off.png"],
        ["Daily","fan_off.png"],
        ["Weekly","fan_off.png"]

    ]

    
    
    
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
        repeatType.setParentView1(self, p: view,menuItemNames: repeatTypeMenuNames);
        quickDaysView.hidden = true
        daysBox.append(day1box);
        daysBox.append(day2box);
        /*daysBox.append(day3box);
        daysBox.append(day4box);
        daysBox.append(day5box);
        daysBox.append(day6box);
        daysBox.append(day7box);*/
        populateDevices();
        
    }
    
    func populateDevices() {
        var dictionary = Dictionary<Int, SelectDeviceView>()
        let lights:[DeviceDAO] = dbOperation.getLights(roomName);
        print("lightcount......\(lights.count)")
        for i in 0 ..< lights.count {
            let selectDeviceComp = SelectDeviceView(frame: deviceView.bounds, deviceDAO: lights[i]);
            deviceView.addArrangedSubview(selectDeviceComp)
            dictionary[lights[i].deviceId] = selectDeviceComp
        }
        let devices:[DeviceDAO] = dbOperation.getDevices(roomName);
        for i in 0 ..< devices.count {
            let selectDeviceComp = SelectDeviceView(frame: deviceView.bounds, deviceDAO: devices[i]);
            deviceView.addArrangedSubview(selectDeviceComp)
            dictionary[devices[i].deviceId] = selectDeviceComp
        }
        schedularTitleLabel.text = "Adding a new schedular";
        if( currentScheduler != nil) {
            
            schedularTitleLabel.text = "Updating \(currentScheduler.name)";
            schedularNameText.text = currentScheduler.name;
            startTimeText.text = currentScheduler.startTime;
            repeatType.setSelecteditem(currentScheduler.repeatType);
            print("")
            if (currentScheduler.repeatType == "Weekly"){
                quickDaysView.hidden = false
             enabledaysinView()
            }
            
            
            //print("types.......\(repeatType.setSelecteditem(currentScheduler.repeatType))")
            startTimeText.text = currentScheduler.startTime;
            let devInfo:[[Int]] = currentScheduler.devicesArray
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
    func enabledaysinView(){
        let charOne:Character = "1"
        let isHavingDaysEnabled = (currentScheduler.repeatValue != "")
        if( isHavingDaysEnabled) {
            var index:Int = 0;
            for i in currentScheduler.repeatValue.characters {
                print(i)
                let checked = (i == charOne)
                if( index<daysBox.count) {
                    daysBox[index].isChecked = checked
                }
                index += 1
            }

        }
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func menuItemClicked(sourceMenu:UIView, rowIndex: Int) {
        print("dsdsds")
        
        quickDaysView.hidden = ( rowIndex != 2)
    }
    
    func setRoomDeviceName(name:String) {
        roomName = name
    }
    
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject?) -> Bool {
        if( identifier != "saveSegueId") {
            return true
        }
        let name = schedularNameText.text
        print("Name from configname...\(name)")
        if( (name == "") ) {
            dbOperation.showAlert(self, message: "Schedular name is empty")
            return false
        }
        return true
    }
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        print("From GrpupViewController seqgueid...\(segue.identifier)")
        if( segue.identifier == "saveSegueId") {
        types = "5";
            print("repea type............\(repeatType.getSelectedText())")
            print("starttiem............\(startTimeText.text)")
            let schedularDao:SchedulerDAO = SchedulerDAO( name: schedularNameText.text! , repeatType: (repeatType.getSelectedText()), startTime: (startTimeText.text!), repeatValue: (repeatType.getSelectedText()));
            
            let numComps = deviceView.arrangedSubviews.count
            for i in 0 ..< numComps {
                let selComp = deviceView.arrangedSubviews[i] as! SelectDeviceView
                if ( selComp.isCompSelected() ) {
                    let devInfo:[Int] = [selComp.deviceDAO.deviceId, selComp.getControllerValue()]
                    schedularDao.devicesArray.append(devInfo)
                    print(" devid.....\(selComp.deviceDAO.deviceId) and cvalue...\(selComp.getControllerValue())")
                }
                
            }
            var repeatValue:String = ""
            for daybox in daysBox {
                if( daybox.isChecked ) {
                    repeatValue += "1"
                } else {
                    repeatValue += "0"
                }
            }
            schedularDao.repeatValue = repeatValue;
            if( currentScheduler == nil) {
                dbOperation.addScheduler(roomName, light: schedularDao)
            } else {
                dbOperation.updateScheduler(roomName, schedular: schedularDao, indexAt: indexAt)
            }
        }
    }
    
    func updateDAO(scheduler: SchedulerDAO, indexAt:Int) {
        print("Update..........DAO  ....\(scheduler.name)")
        currentScheduler = scheduler
        self.indexAt = indexAt
    }
    
}
