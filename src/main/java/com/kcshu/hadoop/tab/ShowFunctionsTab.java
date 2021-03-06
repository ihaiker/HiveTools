package com.kcshu.hadoop.tab;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

import com.kcshu.hadoop.domain.TabType;
import com.kcshu.hadoop.service.ServerManager;
import com.kcshu.hadoop.task.DescribeFunsCallBack;
import com.kcshu.hadoop.task.ShowFunsCallBack;
import com.kcshu.hadoop.utils.i18n;
import com.kcshu.hadoop.utils.images;

/**
 * 
 * @author zhouhaichao(a)2008.sina.com
 * @version 1.0 & 2014年10月20日 上午10:58:23
 */
public class ShowFunctionsTab extends AbstractTab{

    protected Table table;
    public ShowFunctionsTab(CTabFolder tabFolder, TreeItem item){
        super(tabFolder, item);
    }

    @Override
    public void initSubView(Composite com){
        TableColumnLayout tclayout = new TableColumnLayout();
        com.setLayout(tclayout);
        
        table = new Table(com, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        
        TableColumn funName = new TableColumn(table, SWT.NONE);
        funName.setText(i18n.tab.funtab.name);
        tclayout.setColumnData(funName, new ColumnWeightData(1,150, false));
        
        TableColumn funDescription = new TableColumn(table, SWT.NONE);
        funDescription.setText(i18n.tab.funtab.description);
        tclayout.setColumnData(funDescription, new ColumnWeightData(2, 200, false));

        TableColumn funExample = new TableColumn(table, SWT.NONE);
        funExample .setText(i18n.tab.funtab.example);
        tclayout.setColumnData(funExample, new ColumnWeightData(2, 200, false));
        
        
        Listener paintListener = new Listener() {
            public void handleEvent(Event event) {
                switch(event.type) {        
                    case SWT.MeasureItem: {
                        TableItem item = (TableItem)event.item;
                        String text = getText(item, event.index);
                        Point size = event.gc.textExtent(text);
                        event.width = size.x;
                        event.height = Math.max(event.height, size.y);
                        break;
                    }
                    case SWT.PaintItem: {
                        TableItem item = (TableItem)event.item;
                        String text = getText(item, event.index);
                        Point size = event.gc.textExtent(text);                    
                        int offset2 = event.index == 0 ? Math.max(0, (event.height - size.y) / 2) : 0;
                        event.gc.drawText(text, event.x, event.y + offset2, true);
                        break;
                    }
                    case SWT.EraseItem: {    
                        event.detail &= ~SWT.FOREGROUND;
                        break;
                    }
                }
            }
            String getText(TableItem item, int column) {
                return item.getText(column);
            }
        };
       table.addListener(SWT.MeasureItem, paintListener);
       table.addListener(SWT.PaintItem, paintListener);
       table.addListener(SWT.EraseItem, paintListener);
    }

    @Override
    public void afterInitView(){
        executeTask();
        self.setData(TAB_TYPE, TabType.SHOW_FUNCTIONS);
        self.setImage(images.popmenu.server.functions);
    }
    
    @Override
    public void executeTask(){
        ShowFunsCallBack callBack = new ShowFunsCallBack(database){
            @Override
            public void onData(List<String> param){
                showFunInTable(param);
            }
        };
        ServerManager.get(serverId).execute(callBack);
    }
    
    public void showFunInTable(List<String> param){
        for(int i = 0; i < param.size(); i++){
            final int idx = i;
            String funName  = param.get(i);
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0,funName);
            DescribeFunsCallBack callBack = new DescribeFunsCallBack(database,funName){
                @Override
                public void onData(List<String> param){
                    showDescAndExample(idx,param);
                }
            };
            ServerManager.get(serverId).execute(callBack);
        }
    }
    public void showDescAndExample(int idx,List<String> param){
         TableItem item = table.getItem(idx);
         StringBuffer desc = new StringBuffer();
         StringBuffer example = null;
         for(String string : param){
            if(string.startsWith("Example:")){
                example = new StringBuffer();
                continue;
            }
            if(example == null){
                desc.append(string).append("\n");
            }else{
                example.append(string).append("\n");
            }
        }
        item.setText(1,desc.toString());
        if(example != null){
            item.setText(2,example.toString());   
        }
    }

    @Override
    public String getTabTitleType(){
        return i18n.tab.funs;
    }
}
