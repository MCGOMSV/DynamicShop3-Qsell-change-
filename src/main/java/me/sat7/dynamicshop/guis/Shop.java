package me.sat7.dynamicshop.guis;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

import me.sat7.dynamicshop.DynaShopAPI;
import me.sat7.dynamicshop.events.OnChat;
import me.sat7.dynamicshop.utilities.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.sat7.dynamicshop.DynamicShop;
import me.sat7.dynamicshop.constants.Constants;
import me.sat7.dynamicshop.jobshook.JobsHook;
import me.sat7.dynamicshop.transactions.Calc;
import me.sat7.dynamicshop.utilities.ItemsUtil;
import me.sat7.dynamicshop.utilities.LangUtil;
import me.sat7.dynamicshop.utilities.ShopUtil;

public class Shop extends InGameUI {

    public Shop()
    {
        uiType = UI_TYPE.Shop;
    }

    public Inventory getGui(Player player, String shopName, int page) {
        DecimalFormat df = new DecimalFormat("0.00");
        // jobreborn 플러그인 있는지 확인.
        if(!JobsHook.jobsRebornActive && ShopUtil.ccShop.get().contains(shopName+".Options.flag.jobpoint"))
        {
            player.sendMessage(DynamicShop.dsPrefix + LangUtil.ccLang.get().getString("ERR.JOBSREBORN_NOT_FOUND"));
            return null;
        }

        int maxPage = ShopUtil.ccShop.get().getConfigurationSection(shopName).getConfigurationSection("Options").getInt("page");
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;

        String uiName = "";
        if(ShopUtil.ccShop.get().contains(shopName+".Options.title"))
        {
            uiName = ShopUtil.ccShop.get().getString(shopName+".Options.title");
        }
        else
        {
            uiName = shopName;
        }
        Inventory inventory = Bukkit.createInventory(player,54,"§3"+uiName);

        // 닫기 버튼
        ItemStack closeBtn =  ItemsUtil.createItemStack(Material.BARRIER,null,
                LangUtil.ccLang.get().getString("CLOSE"),
                new ArrayList<>(Arrays.asList(LangUtil.ccLang.get().getString("CLOSE_LORE"))),1);

        inventory.setItem(45,closeBtn);

        // 페이지 버튼
        ArrayList<String> pageLore = new ArrayList<>();
        pageLore.add(LangUtil.ccLang.get().getString("PAGE_LORE"));
        if(player.hasPermission("dshop.admin.shopedit"))
        {
            pageLore.add(LangUtil.ccLang.get().getString("PAGE_INSERT"));
            pageLore.add(LangUtil.ccLang.get().getString("PAGE_DELETE"));
        }

        ItemStack pageBtn =  ItemsUtil.createItemStack(Material.PAPER,null,
                page + "/" + maxPage + " " + LangUtil.ccLang.get().getString("PAGE"),
                pageLore,page);

        inventory.setItem(49,pageBtn);

        // 정보,설정 버튼
        String shopLore = LangUtil.ccLang.get().getString("SHOPINFO");

        shopLore = shopLore.replace("[SHOP_NAME]", shopName + "\n");

        String finalLoreText = "";
        if(ShopUtil.ccShop.get().contains(shopName+".Options.lore"))
        {
            String loreTxt = ShopUtil.ccShop.get().getString(shopName + ".Options.lore");
            if(loreTxt.length() > 0)
            {
                String[] loreArray = loreTxt.split(Pattern.quote("\\n"));
                for (String s: loreArray)
                {
                    finalLoreText += "§f" + s + "\n";
                }
            }
        }
        shopLore = shopLore.replace("[SHOP_LORE]",finalLoreText);

        // 권한
        String finalPermText = "";
        String perm = ShopUtil.ccShop.get().getString(shopName+".Options.permission");
        if(!(perm.length()==0))
        {
            finalPermText += LangUtil.ccLang.get().getString("PERMISSION") + ":" + "\n";
            finalPermText += "§7 - " + perm + "\n";
        }
        shopLore = shopLore.replace("[PERMISSION]",finalPermText);

        // 세금
        String finalTaxText = "";
        finalTaxText += LangUtil.ccLang.get().getString("TAX.SALESTAX") + ":" + "\n";
        finalTaxText += "§7 - "+ Calc.getTaxRate(shopName) + "%" + "\n";
        shopLore = shopLore.replace("[TAX]",finalTaxText);

        // 상점 잔액
        String finalShopBalanceText = "";
        finalShopBalanceText += LangUtil.ccLang.get().getString("SHOP_BAL") + "\n";
        if(ShopUtil.getShopBalance(shopName) >= 0)
        {
            String temp = df.format(ShopUtil.getShopBalance(shopName));
            if(ShopUtil.ccShop.get().contains(shopName+".Options.flag.jobpoint")) temp += "Points";

            finalShopBalanceText += "§7 - " + temp + "\n";
        }
        else
        {
            finalShopBalanceText += "§7 - " + ChatColor.stripColor(LangUtil.ccLang.get().getString("SHOP_BAL_INF")) + "\n";
        }
        shopLore = shopLore.replace("[SHOP_BALANCE]",finalShopBalanceText);

        // 영업시간
        String finalShopHourText = "";
        if(ShopUtil.ccShop.get().contains(shopName+".Options.shophours"))
        {
            String[] temp = ShopUtil.ccShop.get().getString(shopName+".Options.shophours").split("~");
            int open = Integer.parseInt(temp[0]);
            int close = Integer.parseInt(temp[1]);

            finalShopHourText += LangUtil.ccLang.get().getString("TIME.SHOPHOURS") + "\n";
            finalShopHourText += "§7 - " + LangUtil.ccLang.get().getString("TIME.OPEN") + ": " + open + "\n";
            finalShopHourText += "§7 - " + LangUtil.ccLang.get().getString("TIME.CLOSE") + ": " + close + "\n";
        }
        shopLore = shopLore.replace("[SHOP_HOUR]",finalShopHourText);

        // 상점 좌표
        String finalShopPosText = "";
        if(ShopUtil.ccShop.get().contains(shopName+".Options.pos1") && ShopUtil.ccShop.get().contains(shopName+".Options.pos2"))
        {
            finalShopPosText += LangUtil.ccLang.get().getString("POSITION") + "\n";
            finalShopPosText += "§7 - "+ ShopUtil.ccShop.get().getString(shopName+".Options.world") + "\n";
            finalShopPosText += "§7 - "+ ShopUtil.ccShop.get().getString(shopName+".Options.pos1") + "\n";
            finalShopPosText += "§7 - "+ ShopUtil.ccShop.get().getString(shopName+".Options.pos2") + "\n";
        }
        shopLore = shopLore.replace("[SHOP_POS]",finalShopPosText);

        // 플래그
        String finalFlagText = "";
        if(ShopUtil.ccShop.get().contains(shopName+".Options.flag") && ShopUtil.ccShop.get().getConfigurationSection(shopName+".Options.flag").getKeys(false).size() > 0)
        {
            finalFlagText += LangUtil.ccLang.get().getString("FLAG") + ":" + "\n";
            for (String s: ShopUtil.ccShop.get().getConfigurationSection(shopName+".Options.flag").getKeys(false))
            {
                finalFlagText += "§7 - " + s + "\n";
            }
        }
        shopLore = shopLore.replace("[FLAG]",finalFlagText);

        // 어드민이면----------
        if(player.hasPermission("dshop.admin.shopedit"))
        {
            shopLore += LangUtil.ccLang.get().getString("RMB_EDIT");
        }

        String infoBtnIconName = ShopUtil.GetShopInfoIconMat();
        ItemStack infoBtn =  ItemsUtil.createItemStack(Material.getMaterial(infoBtnIconName),null, "§3"+shopName, new ArrayList<String>(Arrays.asList(shopLore.split("\n"))),1);
        inventory.setItem(53,infoBtn);

        // 상품목록 등록
        for (String s: ShopUtil.ccShop.get().getConfigurationSection(shopName).getKeys(false))
        {
            try
            {
                // 현재 페이지에 해당하는 것들만 출력
                int idx = Integer.parseInt(s);
                idx -= ((page-1)*45);
                if(!(idx < 45 && idx >= 0)) continue;

                // 아이탬 생성
                String itemName = ShopUtil.ccShop.get().getString(shopName +"."+s+".mat"); // 메테리얼
                ItemStack itemStack = new ItemStack(Material.getMaterial(itemName),1); // 아이탬 생성
                itemStack.setItemMeta((ItemMeta) ShopUtil.ccShop.get().get(shopName + "." + s + ".itemStack")); // 저장된 메타 적용

                // 커스텀 메타 설정
                ItemMeta meta = itemStack.getItemMeta();
                ArrayList<String> lore = new ArrayList<>();

                // 상품
                if(ShopUtil.ccShop.get().contains(shopName+"."+s+".value"))
                {
                    String stockStr;

                    if(ShopUtil.ccShop.get().getInt(shopName+"." + s + ".stock") <= 0)
                    {
                        stockStr = "INF";
                    }
                    else if(DynamicShop.plugin.getConfig().getBoolean("DisplayStockAsStack"))
                    {
                        stockStr = (ShopUtil.ccShop.get().getInt(shopName+"." + s + ".stock")/64)+" Stacks";
                    }
                    else
                    {
                        stockStr = String.valueOf(ShopUtil.ccShop.get().getInt(shopName+"." + s + ".stock"));
                    }

                    double buyPrice = Calc.getCurrentPrice(shopName, s, true);
                    double sellPrice = Calc.getCurrentPrice(shopName, s, false);

                    double buyPrice2 = ShopUtil.ccShop.get().getDouble(shopName+"." + s + ".value");
                    double priceSave1 = ((buyPrice/buyPrice2)*100)-100;
                    double priceSave2 = 100-((buyPrice/buyPrice2)*100);
                    
                    String valueChanged_Buy = null;
                	String valueChanged_Sell = null;
                	
                    if(buyPrice - buyPrice2 > 0) {
                    	valueChanged_Buy = LangUtil.ccLang.get().getString("ARROW_UP") + Math.round(priceSave1*100d)/100d + "%";
                    	valueChanged_Sell = LangUtil.ccLang.get().getString("ARROW_UP") + Math.round(priceSave1*100d)/100d + "%";
                    } else if (buyPrice - buyPrice2 < 0) {
                    	valueChanged_Buy = LangUtil.ccLang.get().getString("ARROW_DOWN") + Math.round(priceSave2*100d)/100d + "%";
                    	valueChanged_Sell = LangUtil.ccLang.get().getString("ARROW_DOWN") + Math.round(priceSave2*100d)/100d + "%";
                    } else if (buyPrice == buyPrice2) {
                    	valueChanged_Buy = "";
                    	valueChanged_Sell = "";
                    }

                    if(buyPrice == sellPrice) sellPrice = buyPrice - ((buyPrice / 100) * Calc.getTaxRate(shopName));

                    String tradeType = "default";
                    if(ShopUtil.ccShop.get().contains(shopName+"."+s+".tradeType")) tradeType = ShopUtil.ccShop.get().getString(shopName+"."+s+".tradeType");

                    boolean showValueChange = ShopUtil.ccShop.get().contains(shopName+".Options.flag.showvaluechange");

                    if(!tradeType.equalsIgnoreCase("SellOnly"))
                        lore.add(LangUtil.ccLang.get().getString("PRICE") + df.format(buyPrice) + (showValueChange ? " " + valueChanged_Buy : ""));

                    if(!tradeType.equalsIgnoreCase("BuyOnly"))
                        lore.add(LangUtil.ccLang.get().getString("SELLPRICE") + df.format(sellPrice) + (showValueChange ? " " + valueChanged_Sell : ""));

                    if(ShopUtil.ccShop.get().getInt(shopName+"." + s + ".stock") <= 0 || ShopUtil.ccShop.get().getInt(shopName+"." + s + ".median") <= 0)
                    {
                        if(!ShopUtil.ccShop.get().getBoolean(shopName+".Options.hidePricingType"))
                        {
                            lore.add("§7[" + ChatColor.stripColor(LangUtil.ccLang.get().getString("STATICPRICE")) + "]");
                        }
                    }
                    if(!ShopUtil.ccShop.get().getBoolean(shopName+".Options.hideStock"))
                    {
                        lore.add(LangUtil.ccLang.get().getString("STOCK") + stockStr);
                    }
                    if(LangUtil.ccLang.get().getString("TRADE_LORE").length() > 0) lore.add(LangUtil.ccLang.get().getString("TRADE_LORE"));

                    if(player.hasPermission("dshop.admin.shopedit"))
                    {
                        lore.add(LangUtil.ccLang.get().getString("ITEM_MOVE_LORE"));
                        lore.add(LangUtil.ccLang.get().getString("ITEM_EDIT_LORE"));
                    }
                }
                // 장식용
                else
                {
                    if(player.hasPermission("dshop.admin.shopedit"))
                    {
                        lore.add(LangUtil.ccLang.get().getString("ITEM_COPY_LORE"));
                        lore.add(LangUtil.ccLang.get().getString("DECO_DELETE_LORE"));
                    }

                    meta.setDisplayName(" ");
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
                }

                meta.setLore(lore);
                itemStack.setItemMeta(meta);
                inventory.setItem(idx,itemStack);
            }
            catch (Exception e)
            {
                if(!s.equalsIgnoreCase("Options"))
                {
                    DynamicShop.console.sendMessage(Constants.DYNAMIC_SHOP_PREFIX +"ERR.OpenShopGui/Failed to create itemstack. incomplete data. check yml.");
//                    for(StackTraceElement ste: e.getStackTrace())
//                    {
//                        DynamicShop.console.sendMessage(DynamicShop.dsPrefix_server+ste);
//                    }
                }
            }
        }
        return inventory;
    }

    @Override
    public void OnClickUpperInventory(InventoryClickEvent e)
    {
        Player player = (Player) e.getWhoClicked();
        if (player == null)
            return;

        String shopName = ChatColor.stripColor(e.getClickedInventory().getItem(53).getItemMeta().getDisplayName());

        int curPage = e.getClickedInventory().getItem(49).getAmount();

        String itemtoMove = "";
        if (DynamicShop.ccUser.get().contains(player.getUniqueId() + ".interactItem"))
        {
            String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId() + ".interactItem").split("/");
            if (temp.length > 1) itemtoMove = temp[1];
        }

        // 빈칸이 아닌 뭔가 클릭함
        if (e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR)
        {
            // 닫기버튼
            if (e.getSlot() == 45)
            {
                SoundUtil.playerSoundEffect(player, "click");

                if (DynamicShop.plugin.getConfig().getBoolean("OnClickCloseButton_OpenStartPage"))
                {
                    DynaShopAPI.openStartPage(player);
                } else
                {
                    ShopUtil.closeInventoryWithDelay(player);
                }
            }
            // 페이지 이동 버튼
            else if (e.getSlot() == 49)
            {
                SoundUtil.playerSoundEffect(player, "click");

                int targetPage = curPage;
                if (e.isLeftClick())
                {
                    if (!e.isShiftClick())
                    {
                        targetPage -= 1;
                        if (targetPage < 1)
                            targetPage = ShopUtil.ccShop.get().getConfigurationSection(shopName).getConfigurationSection("Options").getInt("page");
                    } else if (player.hasPermission("dshop.admin.shopedit"))
                    {
                        ShopUtil.insetShopPage(shopName, curPage);
                    }
                } else if (e.isRightClick())
                {
                    if (!e.isShiftClick())
                    {
                        targetPage += 1;
                        if (targetPage > ShopUtil.ccShop.get().getConfigurationSection(shopName).getConfigurationSection("Options").getInt("page"))
                            targetPage = 1;
                    } else if (player.hasPermission("dshop.admin.shopedit"))
                    {
                        if (ShopUtil.ccShop.get().getInt(shopName + ".Options.page") > 1)
                        {
                            ShopUtil.closeInventoryWithDelay(player);

                            DynamicShop.ccUser.get().set(player.getUniqueId() + ".interactItem", shopName + "/" + curPage);
                            DynamicShop.ccUser.get().set(player.getUniqueId() + ".tmpString", "waitforPageDelete");
                            OnChat.WaitForInput(player);

                            player.sendMessage(DynamicShop.dsPrefix + LangUtil.ccLang.get().getString("RUSURE"));
                        } else
                        {
                            player.sendMessage(DynamicShop.dsPrefix + LangUtil.ccLang.get().getString("CANT_DELETE_LAST_PAGE"));
                        }

                        return;
                    }
                }
                DynaShopAPI.openShopGui(player, shopName, targetPage);
            }
            // 상점 설정 버튼
            else if (e.getSlot() == 53 && e.isRightClick() && player.hasPermission("dshop.admin.shopedit"))
            {
                SoundUtil.playerSoundEffect(player, "click");
                DynamicShop.ccUser.get().set(player.getUniqueId() + ".interactItem", shopName + "/" + 0); // 선택한 아이탬의 인덱스 저장
                DynaShopAPI.openShopSettingGui(player, shopName);
                return;
            } else if (e.getSlot() > 45)
            {
                return;
            }
            // 상점의 아이탬 클릭
            else
            {
                int idx = e.getSlot() + (45 * (curPage - 1));

                // 거래화면 열기
                if (e.isLeftClick())
                {
                    if (!ShopUtil.ccShop.get().contains(shopName + "." + idx + ".value")) return; // 장식용 버튼임

                    SoundUtil.playerSoundEffect(player, "tradeview");
                    DynaShopAPI.openItemTradeGui(player, shopName, String.valueOf(idx));
                    DynamicShop.ccUser.get().set(player.getUniqueId() + ".interactItem", shopName + "/" + idx); // 선택한 아이탬의 인덱스 저장
                }
                // 아이탬 이동, 수정, 또는 장식탬 삭제
                else if (player.hasPermission("dshop.admin.shopedit"))
                {
                    SoundUtil.playerSoundEffect(player, "click");

                    DynamicShop.ccUser.get().set(player.getUniqueId() + ".interactItem", shopName + "/" + idx); // 선택한 아이탬의 인덱스 저장
                    if (e.isShiftClick())
                    {
                        if (ShopUtil.ccShop.get().contains(shopName + "." + idx + ".value"))
                        {
                            double buyValue = ShopUtil.ccShop.get().getDouble(shopName + "." + idx + ".value");
                            double sellValue = buyValue;
                            if (ShopUtil.ccShop.get().contains(shopName + "." + idx + ".value2"))
                            {
                                sellValue = ShopUtil.ccShop.get().getDouble(shopName + "." + idx + ".value2");
                            }
                            double valueMin = ShopUtil.ccShop.get().getDouble(shopName + "." + idx + ".valueMin");
                            if (valueMin <= 0.01) valueMin = 0.01;
                            double valueMax = ShopUtil.ccShop.get().getDouble(shopName + "." + idx + ".valueMax");
                            if (valueMax <= 0) valueMax = -1;
                            int median = ShopUtil.ccShop.get().getInt(shopName + "." + idx + ".median");
                            int stock = ShopUtil.ccShop.get().getInt(shopName + "." + idx + ".stock");

                            ItemStack iStack = new ItemStack(e.getCurrentItem().getType());
                            iStack.setItemMeta((ItemMeta) ShopUtil.ccShop.get().get(shopName + "." + idx + ".itemStack"));

                            DynaShopAPI.openItemSettingGui(player, iStack, 1, buyValue, sellValue, valueMin, valueMax, median, stock);
                        } else
                        {
                            ShopUtil.removeItemFromShop(shopName, idx);
                            DynaShopAPI.openShopGui(player, shopName, idx / 45 + 1);
                        }
                    } else if (itemtoMove.equals(""))
                    {
                        player.sendMessage(DynamicShop.dsPrefix + LangUtil.ccLang.get().getString("ITEM_MOVE_SELECTED"));
                    }
                }
            }
        }
        // 빈칸 클릭함
        else
        {
            if (e.getSlot() > 45)
            {
                return;
            }

            int clickedIdx = e.getSlot() + ((curPage - 1) * 45);

            // 아이탬 이동. 또는 장식 복사
            if (e.isRightClick() && player.hasPermission("dshop.admin.shopedit") && !itemtoMove.equals(""))
            {
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".mat", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".mat"));
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".itemStack", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".itemStack"));
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".value", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".value"));
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".value2", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".value2"));
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".valueMin", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".valueMin"));
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".valueMax", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".valueMax"));
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".median", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".median"));
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".stock", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".stock"));
                ShopUtil.ccShop.get().set(shopName + "." + clickedIdx + ".tradeType", ShopUtil.ccShop.get().get(shopName + "." + itemtoMove + ".tradeType"));

                if (ShopUtil.ccShop.get().contains(shopName + "." + itemtoMove + ".value"))
                {
                    ShopUtil.ccShop.get().set(shopName + "." + itemtoMove, null);
                }

                ShopUtil.ccShop.save();

                DynaShopAPI.openShopGui(player, shopName, curPage);
                DynamicShop.ccUser.get().set(player.getUniqueId() + ".interactItem", "");
            }
            // 팔렛트 열기
            else if (player.hasPermission("dshop.admin.shopedit"))
            {
                DynamicShop.ccUser.get().set(player.getUniqueId() + ".interactItem", shopName + "/" + clickedIdx); // 선택한 아이탬의 인덱스 저장
                DynaShopAPI.openItemPalette(player, 1, "");
            }
        }
    }
}
