//
//  FV.swift
//  second
//
//  Created by Mackbook on 25/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class CollapseView: UIView ,UICollectionViewDelegate, UICollectionViewDelegateFlowLayout, UICollectionViewDataSource {

    var compArray:[CollapseCompData] = [CollapseCompData]()
    @IBOutlet var collectionView: UICollectionView!
    @IBOutlet var minusButton: UIButton!
    @IBOutlet var editButton: UIButton!
    @IBOutlet var plusButton: UIButton!
    @IBOutlet var middleLabel: UILabel!
    @IBOutlet var collapseButton: UIButton!
    var isremoved = false;
    @IBOutlet var contentView: UIView!
    @IBOutlet var rootView: UIStackView!
    var selectedCellIndex:Int = 0;
    var controller:MenuViewController;
    @IBAction func collapseButton(sender: UIButton) {
        if( isremoved) {
            let image = UIImage(named: "downarrow.png")
            sender.setImage(image, forState: .Normal)
            rootView.addArrangedSubview(contentView)
            
        } else {
            let image = UIImage(named: "rightarrow.png")
            sender.setImage(image, forState: .Normal)
            contentView.removeFromSuperview()
        }
        isremoved = !isremoved
        print("Hai")
    }
    var view: UIView!
    
    
    @IBAction func minusAction(sender: UIButton) {
        print("Minus")
    }
    
    @IBAction func plusAction(sender: UIButton) {
        print("Plus")
    }

    @IBAction func editAction(sender: UIButton) {
        print("Edit")
    }
    
    override init(frame: CGRect) {
        // 1. setup any properties here
        self.controller = MenuViewController()
        // 2. call super.init(frame:)
        super.init(frame: frame)
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        self.controller = MenuViewController()
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    init(frame: CGRect, title: String, tag: Int, controller:MenuViewController) {
        self.controller = controller
        super.init(frame: frame)
        xibSetup()
        plusButton.tag = tag
        minusButton.tag = 100+tag
        middleLabel.text = title
        editButton.tag = 200+tag
        collectionView.tag = tag
        minusButton.hidden = true;
        editButton.hidden = true;
        
    }
    
    func xibSetup() {
        view = loadViewFromNib()
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
    }
    
    func loadViewFromNib() -> UIView {
        
        let bundle = NSBundle(forClass: self.dynamicType)
        let nib = UINib(nibName: "CollapseView", bundle: bundle)
        let view = nib.instantiateWithOwner(self, options: nil)[0] as! UIView
        collectionView.dataSource = self
        collectionView.delegate = self
        collectionView.registerClass(ZIconCell.self, forCellWithReuseIdentifier: "Cell")
        collectionView.backgroundColor = UIColor.whiteColor()
        
        return view
    }
    
    func setCompTitle(title: String, tag: Int) {
        plusButton.tag = tag
        middleLabel.text = title
    }
    
    func registerListeners(delegate:AnyObject, methodName:String, iconTappedName:String) {
        plusButton.addTarget(delegate, action: Selector(methodName), forControlEvents: .TouchUpInside)
        minusButton.addTarget(delegate, action: Selector(methodName), forControlEvents: .TouchUpInside)
        editButton.addTarget(delegate, action: Selector(methodName), forControlEvents: .TouchUpInside)
        let lpgr : UILongPressGestureRecognizer = UILongPressGestureRecognizer(target: delegate, action: Selector(iconTappedName))
        self.collectionView.addGestureRecognizer(lpgr)
        
        print("register")
    }
   
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
      //  print("Collection view numcomps...\(compArray.count)")
        return compArray.count;
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("Cell", forIndexPath: indexPath) as! ZIconCell
        cell.setLabelAndImage(compArray[indexPath.row].name, imageName: "about.png")
        return cell;
        //return devicesArray[indexPath.row]
    }
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        let cell = collectionView.cellForItemAtIndexPath(indexPath) as! ZIconCell
        selectedCellIndex = indexPath.row
        print("Selected cellindex.....\(selectedCellIndex)")
        minusButton.hidden = false;
        editButton.hidden = false;
        self.controller.iconClicked(collectionView.tag, rowIndex: selectedCellIndex)
    }
    
    func collectionView(collectionView: UICollectionView, didDeselectItemAtIndexPath indexPath: NSIndexPath) {
        let cell = collectionView.cellForItemAtIndexPath(indexPath) as! ZIconCell
        //cell.toggleSelected()
        print("Deselection cellindex.....\(indexPath.row)")
        minusButton.hidden = true;
        editButton.hidden = true;
    }
    
    func setCellStatus(rowIndex:Int, isOn:Bool) {
        var indexPath = NSIndexPath(forRow: rowIndex, inSection: 0)
        print(" dsdsds....\(collectionView.numberOfSections()).....\(collectionView.numberOfItemsInSection(0))... rowindex...\(rowIndex)")
        let cell = collectionView.cellForItemAtIndexPath(indexPath) as! ZIconCell
        cell.setOn(isOn)
    }

    func clearAllDevices() {
       //   print("creaalllll...\(compArray.count)")
        compArray.removeAll()
      //    print("creaalllll.after..\(compArray.count)")
    }
    
    func addComp(comp:CollapseCompData) {
        compArray.append(comp)
     //   print("FV addcomp numcomps...\(compArray.count)")
        
    }

    func getSelectedIndex()->Int {
        return selectedCellIndex
    }
    
    func removeComp() {
        minusButton.hidden = true;
        editButton.hidden = true;
        compArray.removeAtIndex(selectedCellIndex)
        collectionView.reloadData()
    }
    
    func refresh() {
      //   print("FV refresh...\(compArray.count)")
        collectionView.reloadData()
    }
}
