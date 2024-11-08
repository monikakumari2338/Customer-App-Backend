package com.deepanshu.modal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.*;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private int price;

    @Column(name = "discounted_price")
    private int discountedPrice;

    @Column(name = "discount_persent")
    private int discountPercent;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "brand")
    private String brand;

    @Column(name = "color")
    private String color;

    @Embedded
    @ElementCollection
    @Column(name = "sizes")
    private Set<Size> sizes = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private Set<String> imageUrl = new HashSet<>();

    @Column(name = "country")
    private String country;
    @Column(name = "wearType")
    private String wearType;
    @Column(name = "fabric")
    private String fabric;
    @Column(name = "sleeves")
    private String sleeves;
    @Column(name = "fit")
    private String fit;
    @Column(name = "materialCare")
    private String materialCare;
    @Column(name = "productCode")
    private String productCode;
    @Column(name = "seller")
    private String seller;


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @Column(name = "num_ratings")
    private int numRatings;


    @ManyToOne()
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "pincode_id")
    private Pincode pincode;

    @Column(name = "ingredient")
    private String ingredient;

    @Column(name = "packaging")
    private String packaging;

    @Column(name = "milk_type")
    private String milktype;

    @Column(name = "generic_name")
    private String genericname;

    @Column(name = "country_of_origin")
    private String countryoforigin;

    @Column(name = "preservatives")
    private String preservatives;

    @Column(name = "consume_within")
    private String consumewithin;

    private LocalDateTime createdAt;

    public Product() {

    }

    public Product(Long id, String title, String description, int price, int discountedPrice,
                   int discountPercent, int quantity, String brand, String color,
                   Set<Size> sizes, Set<String> imageUrl, String country, String wearType,
                   String fabric, String sleeves, String fit, String materialCare,
                   String productCode, String seller, List<Rating> ratings, List<Review> reviews,
                   int numRatings, Category category, LocalDateTime createdAt, String ingredient,
                   String packaging, String milktype, String genericname, String countryoforigin,
                   String preservatives, String consumewithin) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.discountedPrice = discountedPrice;
        this.discountPercent = discountPercent;
        this.quantity = quantity;
        this.brand = brand;
        this.color = color;
        this.sizes = sizes;
        this.imageUrl = imageUrl;
        this.country = country;
        this.wearType = wearType;
        this.fabric = fabric;
        this.sleeves = sleeves;
        this.fit = fit;
        this.materialCare = materialCare;
        this.productCode = productCode;
        this.seller = seller;
        this.ratings = ratings;
        this.reviews = reviews;
        this.numRatings = numRatings;
        this.category = category;
        this.createdAt = createdAt;
        this.ingredient = ingredient;
        this.packaging = packaging;
        this.milktype=milktype;
        this.genericname=genericname;
        this.countryoforigin=countryoforigin;
        this.preservatives=preservatives;
        this.consumewithin=consumewithin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Pincode getPincode() {
        return pincode;
    }

    public void setPincode(Pincode pincode) {
        this.pincode = pincode;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(int discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Set<Size> getSizes() {
        return sizes;
    }

    public void setSizes(Set<Size> sizes) {
        this.sizes = sizes;
    }

    public Set<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Set<String> imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getWearType() {
        return wearType;
    }

    public void setWearType(String wearType) {
        this.wearType = wearType;
    }

    public String getFabric() {
        return fabric;
    }

    public void setFabric(String fabric) {
        this.fabric = fabric;
    }

    public String getSleeves() {
        return sleeves;
    }

    public void setSleeves(String sleeves) {
        this.sleeves = sleeves;
    }

    public String getFit() {
        return fit;
    }

    public void setFit(String fit) {
        this.fit = fit;
    }

    public String getMaterialCare() {
        return materialCare;
    }

    public void setMaterialCare(String materialCare) {
        this.materialCare = materialCare;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public String getMilktype() {
        return milktype;
    }

    public void setMilktype(String milktype) {
        this.milktype = milktype;
    }

    public String getGenericname() {
        return genericname;
    }

    public void setGenericname(String genericname) {
        this.genericname = genericname;
    }

    public String getCountryoforigin() {
        return countryoforigin;
    }

    public void setCountryoforigin(String countryoforigin) {
        this.countryoforigin = countryoforigin;
    }

    public String getPreservatives() {
        return preservatives;
    }

    public void setPreservatives(String preservatives) {
        this.preservatives = preservatives;
    }

    public String getConsumewithin() {
        return consumewithin;
    }

    public void setConsumewithin(String consumewithin) {
        this.consumewithin = consumewithin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(brand, category, color, description, discountPercent, discountedPrice, id, imageUrl,
                numRatings, price, quantity, ratings, reviews, sizes, title,
                country, wearType, fabric, sleeves, fit, materialCare, productCode, seller);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Product other = (Product) obj;
        return Objects.equals(brand, other.brand) && Objects.equals(category, other.category)
                && Objects.equals(color, other.color) && Objects.equals(description, other.description)
                && discountPercent == other.discountPercent && discountedPrice == other.discountedPrice
                && Objects.equals(id, other.id) && Objects.equals(imageUrl, other.imageUrl)
                && numRatings == other.numRatings && price == other.price && quantity == other.quantity
                && Objects.equals(ratings, other.ratings) && Objects.equals(reviews, other.reviews)
                && Objects.equals(sizes, other.sizes) && Objects.equals(title, other.title);
    }

    public void increaseInventory(String sizeName, int quantity) {
        Size size = findSizeByName(sizeName);
        if (size != null) {
            size.increaseQuantity(quantity);
        } else {
            throw new IllegalArgumentException("Size " + sizeName + " does not exist for this product");
        }
    }

    public void decreaseInventory(String sizeName, int quantity) {
        Size size = findSizeByName(sizeName);
        if (size != null) {
            size.decreaseQuantity(quantity);
        } else {
            throw new IllegalArgumentException("Size " + sizeName + " does not exist for this product");
        }
    }

    private Size findSizeByName(String sizeName) {
        for (Size size : sizes) {
            if (size.getName().equals(sizeName)) {
                return size;
            }
        }
        return null;
    }

}
