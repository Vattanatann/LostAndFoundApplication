package com.tann.vattana.lostandfoundapplication.Class;

public class LostItemInfo {

    private String itemTitle;
    private String foundAddress;
    private String foundDate;
    private String postDate;
    private String founderContact;
    private String itemDescription;
    private String founder;
    private boolean itemStatus;
    private String itemImageUrl;
    private String pickUpAddress;
    private String pickUpContact;
    private String founderID;
    private String itemID;

    public LostItemInfo (String itemTitle, String foundAddress, String foundDate, String postDate, String founderContact, String itemDescription, String founder, boolean itemStatus,
                         String pickUpAddress, String pickUpContact, String founderID, String itemImageUrl, String itemID) {
        this.itemTitle = itemTitle;
        this.foundAddress = foundAddress;
        this.foundDate = foundDate;
        this.postDate = postDate;
        this.founderContact = founderContact;
        this.itemDescription = itemDescription;
        this.founder = founder;
        this.itemStatus = itemStatus;
        this.pickUpAddress = pickUpAddress;
        this.pickUpContact = pickUpContact;
        this.founderID = founderID;
        this.itemImageUrl = itemImageUrl;
        this.itemID = itemID;
    }

    public LostItemInfo () {

    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getFoundAddress() {
        return foundAddress;
    }

    public void setFoundAddress(String foundAddress) {
        this.foundAddress = foundAddress;
    }

    public String getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(String foundDate) {
        this.foundDate = foundDate;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getFounderContact() {
        return founderContact;
    }

    public void setFounderContact(String founderContact) {
        this.founderContact = founderContact;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getFounder() {
        return founder;
    }

    public void setFounder(String founder) {
        this.founder = founder;
    }

    public boolean isItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(boolean itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        this.itemImageUrl = itemImageUrl;
    }

    public String getPickUpAddress() {
        return pickUpAddress;
    }

    public void setPickUpAddress(String pickUpAddress) {
        this.pickUpAddress = pickUpAddress;
    }

    public String getPickUpContact() {
        return pickUpContact;
    }

    public void setPickUpContact(String pickUpContact) {
        this.pickUpContact = pickUpContact;
    }

    public String getFounderID() {
        return founderID;
    }

    public void setFounderID(String founderID) {
        this.founderID = founderID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }
}
