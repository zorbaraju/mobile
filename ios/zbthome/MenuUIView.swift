//
//  MenuUIView.swift
//  second
//
//  Created by Mackbook on 27/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class MenuUIView: UIView, UITableViewDataSource, UITableViewDelegate{
    
    var menudelegate:ViewController!
    var tablerowheight:CGFloat = 40;
    var maxcellwidth:CGFloat = 0;
    var isimagemenu:Bool = false;
    var parentView:UIView!;
    var menu:MenuUIView!;
    var menuNames: [String]!
    var c:String!
    let cellReuseIdentifier = "cell"
    
    @IBOutlet var menuTableView: UITableView!
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
        view.autoresizingMask = [UIViewAutoresizing.FlexibleHeight]
        // Adding custom subview on top of our view (over any custom drawing > see note below)
        addSubview(view)
        
        menuTableView.dataSource = self;
        menuTableView.delegate = self;
        self.menuTableView.registerClass(UITableViewCell.self, forCellReuseIdentifier: cellReuseIdentifier)
        menuTableView.hidden = true
        menuTableView.removeFromSuperview()
    }
    
    func loadViewFromNib() -> UIView {
        let bundle = NSBundle(forClass: self.dynamicType)
        let nib = UINib(nibName: "MenuUIView", bundle: bundle)
        let view = nib.instantiateWithOwner(self, options: nil)[0] as! UIView
        return view
    }
    
    func setMenuItems(menuItems: [String]) {
        self.menuNames = menuItems
    }
    // number of rows in table view
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.menuNames.count
    }
    
    // create a cell for each table view row
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        
        // create a new cell if needed or reuse an old one
        let cell:UITableViewCell = self.menuTableView.dequeueReusableCellWithIdentifier(cellReuseIdentifier) as UITableViewCell!
        
        cell.backgroundColor = UIColor.blackColor()
        cell.textLabel?.textColor = UIColor.whiteColor()
        // set the text from the data model
        cell.textLabel?.text = self.menuNames[indexPath.row]
        cell.contentView.layer.cornerRadius = 3
        cell.contentView.layer.masksToBounds = true
        
        cell.layer.borderColor = UIColor.grayColor().CGColor
        cell.layer.borderWidth = 1
        cell.frame = CGRectMake(cell.frame.origin.x, cell.frame.origin.y, cell.bounds.width, tablerowheight);
        let v = cell.textLabel!.font.lineHeight*CGFloat(menuNames[indexPath.row].characters.count);
        
        if( maxcellwidth < v ) {
            maxcellwidth = v
        }
        print("maxcellwidth \(maxcellwidth)")
        return cell
    }
    
    // method to run when table view cell is tapped
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        print("You tapped cell number \(indexPath.row) \(self.menudelegate).")
        if( !isimagemenu ) {
            menuButton.setTitle(menuNames[indexPath.row], forState: .Normal)
        }
        menuTableView.hidden = true;
        self.menudelegate.menuItemClicked(self, rowIndex: indexPath.row)
    }
    
    func setParentView1(delegate:ViewController, p:UIView, menuItemImages: [UIImage], menuItemNames: [String]) {
        self.menudelegate = delegate;
        parentView = p;
        parentView.addSubview(menuTableView)
        menuNames = menuItemNames;
    }
    
    func setSelecteditem(item:String) {
        c = item
        menuButton.titleLabel?.text = c;
        print( "setSelecteditem..............\(c)")
        
    }
    
    @IBAction func menuButtonClicked(sender: UIButton) {
        print("lallll\(menuButton.titleLabel?.text)")
        if( menuTableView.hidden) {
            menuTableView.hidden = false;
        } else {
            menuTableView.hidden = true;
        }
        print("llllbl\(menuButton.titleLabel?.text)")
    }
    
    override func layoutSubviews() {
        //super.layoutSubviews()
        menuTableView.rowHeight = UITableViewAutomaticDimension
        menuTableView.estimatedRowHeight = tablerowheight+5
        let th = menuTableView.estimatedRowHeight*CGFloat(menuNames.count)+2
        let tw = maxcellwidth
        self.menuTableView.frame = CGRectMake(frame.origin.x, frame.origin.y+frame.size.height, tw, th)
        print("lllll\(menuTableView.estimatedRowHeight).....\(c)")
        print("setting titlelaelllll \(c)")
        menuButton.setTitle(c, forState: .Normal)
    }
    
    func setImageButton(image: UIImage) {
        isimagemenu  = true
        menuButton.setTitle("", forState: .Normal)
        menuButton.setImage(image, forState: .Normal)
    }

}
