//
//  AboutViewController.swift
//  second
//
//  Created by Mackbook on 28/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class DiscoveryViewController: UIViewController ,UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {

    @IBOutlet var configuredView: UICollectionView!
    
    @IBOutlet var discoveredDevicesPanel: UIStackView!
    var discoveredDevices:[LabelTextView] = [LabelTextView]()
    
    @IBOutlet var scrollView: UIScrollView!
    @IBOutlet var verticalview: UIStackView!
    let phoneMemory = NSUserDefaults.standardUserDefaults()
    override func viewDidLoad() {
        super.viewDidLoad()

        scrollView.translatesAutoresizingMaskIntoConstraints = false;
        //scrollView.addSubview(view)
        scrollView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("V:|[verticalview]|", options: NSLayoutFormatOptions(rawValue:0),metrics: nil, views: ["verticalview":verticalview]))
        scrollView.addConstraints(NSLayoutConstraint.constraintsWithVisualFormat("H:|[verticalview]|", options: NSLayoutFormatOptions(rawValue:0),metrics: nil, views: ["verticalview":verticalview]))
        scrollView.addConstraint(
            NSLayoutConstraint(item: scrollView,
                attribute: .Width,
                relatedBy: .Equal,
                toItem: verticalview,
                attribute: .Width,
                multiplier: 1.0,
                constant: 0))
        
        configuredView.dataSource = self
        configuredView.delegate = self
        configuredView.registerClass(ZIconCell.self, forCellWithReuseIdentifier: "Cell")
        configuredView.backgroundColor = UIColor.whiteColor()
        
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(DiscoveryViewController.goback))
        view.addGestureRecognizer(tap) // Do any additional setup after loading the view.
        
        let lt:LabelTextView = LabelTextView(frame: discoveredDevicesPanel.bounds, title:"Zorba1");
        discoveredDevicesPanel.addArrangedSubview(lt)
        discoveredDevices.append(lt)
    }
   
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        print("Collection view numcomps...\(1)")
        return 10;
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("Cell", forIndexPath: indexPath) as! ZIconCell
        cell.backgroundColor = UIColor.orangeColor()
        cell.setLabelAndImage("Raju", imageName: "about.png")
        return cell
    }
    
    func goback() {
         print("gesture activity")
        performSegueWithIdentifier("goBackFromRoomAdd", sender: nil)
    }
    
    @IBAction func addRoomAction(sender: UIButton) {
        if (discoveredDevices.count>0)  {
            let lt:LabelTextView = discoveredDevices[0];
            let inputName = lt.getValue();
            if(inputName.isEmpty) {
                print("Room name is empty")
                return;
            } else {
                let newroom:RoomDAO = RoomDAO(deviceName: "d", roomName: inputName)
                DBOperation().addRoom(newroom);
                performSegueWithIdentifier("goBackFromRoomAdd", sender: nil)
            }
        }
       
    }

}
