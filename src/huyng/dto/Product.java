package huyng.dto;

import java.math.BigInteger;

public class Product {
    private Long ProductId;
    private String ProductName;
    private BigInteger ProductPrice;
    private String thumbnail;
    private String CategoryId;
    private boolean IsActive;
    private String DetailLink;

    public Product(Long productId, String productName, BigInteger productPrice, String thumbnail, String categoryId, boolean isActive, String detailLink) {
        ProductId = productId;
        ProductName = productName;
        ProductPrice = productPrice;
        this.thumbnail = thumbnail;
        CategoryId = categoryId;
        IsActive = isActive;
        this.DetailLink = detailLink;
    }

    public Long getProductId() {
        return ProductId;
    }

    public void setProductId(Long productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public BigInteger getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(BigInteger productPrice) {
        ProductPrice = productPrice;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(String categoryId) {
        CategoryId = categoryId;
    }

    public boolean isActive() {
        return IsActive;
    }

    public void setActive(boolean active) {
        IsActive = active;
    }

    public String getDetailLink() {
        return DetailLink;
    }

    public void setDetailLink(String detailLink) {
        DetailLink = detailLink;
    }
}
