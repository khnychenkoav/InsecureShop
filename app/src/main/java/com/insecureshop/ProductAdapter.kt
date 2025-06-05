package com.insecureshop

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.insecureshop.databinding.ProductItemBinding
import com.insecureshop.util.Util

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    private val TAG_ADAPTER = "ProductAdapter_DEBUG"

    var productList : List<ProductDetail> = arrayListOf()

    class ProductViewHolder(binding: ProductItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var mBinding = binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ProductItemBinding.inflate(LayoutInflater.from(parent.context))
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val prodDetail = productList[position]
        val context = holder.mBinding.root.context
        Glide.with(holder.mBinding.picture.context).load(prodDetail.imageUrl)
            .placeholder(ContextCompat.getDrawable(context, R.mipmap.ic_launcher))
            .into(holder.mBinding.picture)
        holder.mBinding.prodName.text = prodDetail.name
        holder.mBinding.productCount.text = prodDetail.qty.toString()
        val ctx = holder.mBinding.root.context
        holder.mBinding.prodPrice.text = ctx.getString(
            R.string.price_format,
            prodDetail.price
        )
        holder.mBinding.icAdd.setOnClickListener {
            prodDetail.qty += 1
            holder.mBinding.productCount.text = prodDetail.qty.toString()
            Util.updateProductItem(context, prodDetail)
        }
        holder.mBinding.icRemove.setOnClickListener {
            if (prodDetail.qty > 0) {
                prodDetail.qty -= 1
                holder.mBinding.productCount.text = prodDetail.qty.toString()
                Util.updateProductItem(context, prodDetail)
            }
        }
        holder.mBinding.moreInfo.setOnClickListener {
            Log.d(TAG_ADAPTER, "moreInfo CLICKED for product: ${prodDetail.name}")

            val intent = Intent("com.insecureshop.action.PRODUCT_DETAIL").apply {
                putExtra("url", prodDetail.url)
                setPackage(context.packageName)
            }
            Log.d(TAG_ADAPTER, "Sending broadcast with URL: ${prodDetail.url}")
            context.sendBroadcast(intent)

        }
    }
}