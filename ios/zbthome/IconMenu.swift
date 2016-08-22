//
//  MenuUIView.swift
//  second
//
//  Created by Mackbook on 27/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class IconMenu: UIView,UICollectionViewDelegate, UICollectionViewDelegateFlowLayout, UICollectionViewDataSource{
    
    @IBOutlet var collectionMenuView: UICollectionView!
    var menudelegate:MenuViewController!
    var tablerowheight:CGFloat = 40;
    var maxcellwidth:CGFloat = 0;
    var parentView:UIView!;
    var menu:MenuUIView!;
    var menuNames: [[String]]!
    var selectedItemName:String!
    let cellReuseIdentifier = "cell"
    
    @IBOutlet var menuButton: UIButton!
    var view: UIView!
  
    init(frame: CGRect,title:String, tag:Int) {
        // 1. setup any properties here
        
        // 2. call super.init(frame:)
        super.init(frame: frame)
        
        // 3. Setup view from .xib file
        xibSetup()
        print("init frame")
    }
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
        print("init coder")
    }
    
    func xibSetup() {
        
        view = loadViewFromNib()
        
        // use bounds not frame or it'll be offset
        view.frame = bounds
        
        // Make the view stretch with containing view
        view.autoresizingMask = [UIViewAutoresizing.FlexibleWidth, UIViewAutoresizing.FlexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
        
        collectionMenuView.dataSource = self;
        collectionMenuView.delegate = self;
        collectionMenuView.registerClass(ZIconCell.self, forCellWithReuseIdentifier: "IConCell")
        collectionMenuView.hidden = true
        collectionMenuView.removeFromSuperview()
    }
    
    func loadViewFromNib() -> UIView {
        let bundle = NSBundle(forClass: self.dynamicType)
        let nib = UINib(nibName: "IconMenu", bundle: bundle)
        let view = nib.instantiateWithOwner(self, options: nil)[0] as! UIView
        menuButton.setTitle("", forState: .Normal)
        return view
    }
    
    func setMenuItems(menuItems: [[String]]) {
        self.menuNames = menuItems
        collectionMenuView.reloadData()
        menuButton.setTitle(menuNames[0][0], forState: .Normal)
    }
    
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        return self.menuNames.count
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("IConCell", forIndexPath: indexPath) as! ZIconCell
        cell.setLabelAndImage(self.menuNames[indexPath.row][0], imageName: self.menuNames[indexPath.row][1])
        return cell;
    }
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        menuButton.setTitle(menuNames[indexPath.row][0], forState: .Normal)
        collectionMenuView.hidden = true;
        self.menudelegate.menuItemClicked(self, rowIndex: indexPath.row)
    }

   
    func setParentView1(delegate:MenuViewController, p:UIView, menuItemNames: [[String]]) {
        self.menudelegate = delegate;
        parentView = p;
        parentView.addSubview(collectionMenuView)
        menuNames = menuItemNames;
    }
    
    func clearSelection() {
        selectedItemName = menuNames[0][0]
        menuButton.setTitle(menuNames[0][0], forState: .Normal)
    }
    
    func setSelecteditem(item:String) {
        selectedItemName = item
        menuButton.setTitle(selectedItemName, forState: .Normal)
    }
    
    func getSelectedText()-> String{
        let name = menuButton.titleLabel?.text
        return name!;
    }
    
    @IBAction func menuButtonClicked(sender: UIButton) {
        if( collectionMenuView.hidden) {
            collectionMenuView.hidden = false;
        } else {
            collectionMenuView.hidden = true;
        }
    }
    
    override func layoutSubviews() {
        //super.layoutSubviews()
        /*collectionMenuView.row.rowHeight = UITableViewAutomaticDimension
        collectionMenuView.estimatedRowHeight = tablerowheight+5
        let th = collectionMenuView.estimatedRowHeight*CGFloat(menuNames.count)+2
        let tw = maxcellwidth
        self.collectionMenuView.frame = CGRectMake(frame.origin.x, frame.origin.y+frame.size.height, tw, th)
        print("lllll\(collectionMenuView.estimatedRowHeight).....\(c)")*/
        menuButton.setTitle(selectedItemName, forState: .Normal)
    }
    
}
