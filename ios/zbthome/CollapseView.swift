//
//  FV.swift
//  second
//
//  Created by Mackbook on 25/07/16.
//  Copyright © 2016 Mackbook. All rights reserved.
//

import UIKit

class CollapseView: UIView ,UICollectionViewDelegate, UICollectionViewDelegateFlowLayout, UICollectionViewDataSource{

    var compArray:[CollapseCompData] = [CollapseCompData]()
    @IBOutlet var collectionView: UICollectionView!
    @IBOutlet var minusButton: UIButton!
    @IBOutlet var plusButton: UIButton!
    @IBOutlet var middleLabel: UILabel!
    @IBOutlet var collapseButton: UIButton!
    var isremoved = false;
    @IBOutlet var contentView: UIView!
    @IBOutlet var rootView: UIStackView!
    var selectedCellIndex:Int = 0;
    
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
    
    override init(frame: CGRect) {
        // 1. setup any properties here
        
        // 2. call super.init(frame:)
        super.init(frame: frame)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    required init?(coder aDecoder: NSCoder) {
        // 1. setup any properties here
        
        // 2. call super.init(coder:)
        super.init(coder: aDecoder)
        
        // 3. Setup view from .xib file
        xibSetup()
    }
    
    init(frame: CGRect, title: String, tag: Int) {
        
        super.init(frame: frame)
        xibSetup()
        plusButton.tag = tag
        minusButton.tag = 100+tag
        middleLabel.text = title
        minusButton.hidden = true;
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
    
    func registerListeners(delegate:AnyObject, methodName:String) {
        plusButton.addTarget(delegate, action: Selector(methodName), forControlEvents: .TouchUpInside)
        minusButton.addTarget(delegate, action: Selector(methodName), forControlEvents: .TouchUpInside)
    print("register")
    }
   
    func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
        print("Collection view numcomps...\(compArray.count)")
        return compArray.count;
    }
    
    func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
        let cell = collectionView.dequeueReusableCellWithReuseIdentifier("Cell", forIndexPath: indexPath) as! ZIconCell
        cell.setLabelAndImage(compArray[indexPath.row].name, imageName: "about.png")
        return cell;
        //return devicesArray[indexPath.row]
    }
    func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
        collectionView.deselectItemAtIndexPath(indexPath, animated: false)
        selectedCellIndex = indexPath.row
        print("Selected cellindex.....\(selectedCellIndex)")
        minusButton.hidden = false;
    }

    func clearAllDevices() {
          print("creaalllll...\(compArray.count)")
        compArray.removeAll()
          print("creaalllll.after..\(compArray.count)")
    }
    
    func addComp(comp:CollapseCompData) {
        compArray.append(comp)
        print("FV addcomp numcomps...\(compArray.count)")
        
    }

    func getSelectedIndex()->Int {
        return selectedCellIndex
    }
    
    func removeComp() {
        minusButton.hidden = true;
        compArray.removeAtIndex(selectedCellIndex)
        collectionView.reloadData()
    }
    
    func refresh() {
         print("FV refresh...\(compArray.count)")
        collectionView.reloadData()
    }
}

/*
 class ViewController : UIViewController, UICollectionViewDelegate, UICollectionViewDataSource, UICollectionViewDelegateFlowLayout {
     private let barSize : CGFloat = 44.0
     private let kCellReuse : String = "PackCell"
     private let kCellheaderReuse : String = "PackHeader"
     private var collectionView : UICollectionView = UICollectionView(frame: CGRectZero, collectionViewLayout: UICollectionViewFlowLayout())   // Initialization
  
     override func viewDidLoad() {
         super.viewDidLoad()
  
         // Collection
         self.collectionView.delegate = self     // delegate  :  UICollectionViewDelegate
         self.collectionView.dataSource = self   // datasource  : UICollectionViewDataSource
         self.collectionView.backgroundColor = UIColor.clearColor()
  
         // Register parts(header and cell
         self.collectionView.registerClass(PackViewCell.self, forCellWithReuseIdentifier: kCellReuse) // UICollectionViewCell
         self.collectionView.registerClass(PackCollectionSectionView.self, forSupplementaryViewOfKind: UICollectionElementKindSectionHeader, withReuseIdentifier: kCellheaderReuse)  // UICollectionReusableView
          
         self.view.addSubview(self.collectionView)
     }
  
     override func viewWillLayoutSubviews() {
         let frame = self.view.frame
         self.collectionView.frame = CGRectMake(frame.origin.x, frame.origin.y + barSize, frame.size.width, frame.size.height - barSize)
     }
  
     // MARK: UICollectionViewDelegate, UICollectionViewDataSource
     func collectionView(collectionView: UICollectionView, cellForItemAtIndexPath indexPath: NSIndexPath) -> UICollectionViewCell {
         var cell : PackViewCell = collectionView.dequeueReusableCellWithReuseIdentifier(kCellReuse, forIndexPath: indexPath) as PackViewCell
         return cell    // Create UICollectionViewCell
     }
  
     func numberOfSectionsInCollectionView(collectionView: UICollectionView) -> Int {
         return 1  // Number of section
     }
  
     func collectionView(collectionView: UICollectionView, numberOfItemsInSection section: Int) -> Int {
         var res = 0
         switch(section) {
         case 0:
             res = 4  // Number of cell per section(section 0)
             break
         default:
             res = 0
             break
         }
         return res
     }
  
     func collectionView(collectionView: UICollectionView, didSelectItemAtIndexPath indexPath: NSIndexPath) {
         collectionView.deselectItemAtIndexPath(indexPath, animated: false)
         // Select operation
     }
  
     func collectionView(collectionView: UICollectionView, viewForSupplementaryElementOfKind kind: String, atIndexPath indexPath: NSIndexPath) -> UICollectionReusableView {
         var reusableView : UICollectionReusableView? = nil
          
         // Create header
         if (kind == UICollectionElementKindSectionHeader) {
             // Create Header
             var headerView : PackCollectionSectionView = collectionView.dequeueReusableSupplementaryViewOfKind(UICollectionElementKindSectionHeader, withReuseIdentifier: kCellheaderReuse, forIndexPath: indexPath) as PackCollectionSectionView
              
             reusableView = headerView
         }
         return reusableView!
     }
  
     // MARK: UICollectionViewDelegateFlowLayout
     func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, sizeForItemAtIndexPath indexPath: NSIndexPath) -> CGSize {
         return CGSize(width: 90, height: 90) // The size of one cell
     }
  
     func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, referenceSizeForHeaderInSection section: Int) -> CGSize {
         return CGSizeMake(self.view.frame.width, 90)  // Header size
     }
  
     func collectionView(collectionView: UICollectionView, layout collectionViewLayout: UICollectionViewLayout, insetForSectionAtIndex section: Int) -> UIEdgeInsets {
         let frame : CGRect = self.view.frame
         let margin  = (frame.width - 90 * 3) / 6.0
         return UIEdgeInsetsMake(10, margin, 10, margin) // margin between cells
     }
 }
 */
